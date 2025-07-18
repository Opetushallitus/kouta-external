package fi.oph.kouta.europass

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.domain.indexed.{
  ToteutusIndexed,
  KoulutusIndexed,
  Organisaatio
}
import scala.xml.Elem
import java.io.{File, BufferedWriter, FileWriter}

class Publisher(converter: EuropassConversion) extends Logging {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val initialLogDelayInItems = 10
  val subsequentLogDelayInItems = 200
  val limitToteutukset = EuropassConfiguration.config.getBoolean(
    "europass-publisher.retrieval.use-toteutus-limit"
  )
  val toteutusLimit = EuropassConfiguration.config.getInt(
    "europass-publisher.retrieval.toteutus-limit"
  )

  def toteutusToFile(oid: String, dest: BufferedWriter) = {
    val Some(toteutusXml: Elem) = converter.toteutusAsElmXml(ElasticClient.getToteutus(oid))
    dest.write(
      <Courses xmlns="http://data.europa.eu/snb/model/ap/loq-constraints/">
        <learningOpportunityReferences>
          {toteutusXml}
        </learningOpportunityReferences>
      </Courses>.toString
    )
    dest.close()
  }

  def foreachWithLogging(seq: Stream[Elem], kind: String, handle: Elem => Unit) = {
    var writtenCount = 0;
    seq.foreach {item =>
      handle(item)
      writtenCount += 1
      if (writtenCount % subsequentLogDelayInItems == initialLogDelayInItems) {
        logger.info(s"written $writtenCount $kind to file")
      }
    }
  }

  def koulutuksetToFile(dest: BufferedWriter, koulutusStream: Stream[KoulutusIndexed]) = {
    dest.write("<learningAchievementSpecificationReferences>\n")
    foreachWithLogging(
      koulutusStream.flatMap(converter.koulutusAsElmXml),
      "koulutukset",
      {koulutus => dest.write(koulutus.toString)}
    )
    dest.write("</learningAchievementSpecificationReferences>\n")
  }

  def koulutusDependentsOfToteutukset(
    toteutusStream: Stream[ToteutusIndexed]
  ): Stream[KoulutusIndexed] =
    toteutusStream
      .flatMap(converter.toteutusToKoulutusDependents)
      .toSet
      .toStream
      .map(ElasticClient.getKoulutus)

  def toteutuksetToFile(dest: BufferedWriter, toteutusStream: Stream[ToteutusIndexed]) = {
    val toteutusXmlStream = toteutusStream.flatMap(converter.toteutusAsElmXml)
    dest.write("<learningOpportunityReferences>\n")
    foreachWithLogging(
      toteutusXmlStream,
      "toteutukset",
      {toteutus => dest.write(toteutus.toString)}
    )
    dest.write("</learningOpportunityReferences>\n")
  }

  def tuloksetToFile(dest: BufferedWriter, koulutusStream: Stream[KoulutusIndexed]) = {
    val tulosXmlStream = koulutusStream
      .flatMap(converter.koulutusTuloksetAsElmXml)
      .groupBy(_ \@ "id").values.map(_(0)).toStream
    dest.write("<learningOutcomeReferences>\n")
    foreachWithLogging(
      tulosXmlStream,
      "koulutuksen tulokset",
      {tulos => dest.write(tulos.toString)}
    )
    dest.write("</learningOutcomeReferences>\n")
  }

  def tarjoajaDependentsOfToteutukset(
    toteutusStream: Stream[ToteutusIndexed]
  ): Stream[Organisaatio] =
    toteutusStream
    .flatMap(converter.toteutusToTarjoajaDependents)
    .toSet
    .toStream

  def tarjoajatToFile(dest: BufferedWriter, tarjoajaStream: Stream[Organisaatio]) = {
    val organisaatioXmlStream = tarjoajaStream.map(converter.tarjoajaAsElmXml)
    dest.write("<agentReferences>\n")
    foreachWithLogging(
      organisaatioXmlStream,
      "koulutuksen tarjoajat",
      {org => dest.write(org.toString)}
    )
    dest.write("</agentReferences>\n")
  }

  def tarjoajasijainnitToFile(dest: BufferedWriter, tarjoajaStream: Stream[Organisaatio]) = {
    val sijaintiStream = tarjoajaStream.map(converter.tarjoajasijaintiAsElmXml)
    dest.write("<locationReferences>\n")
    foreachWithLogging(
      sijaintiStream,
      "organisaation sijainnit",
      {loc => dest.write(loc.toString)}
    )
    dest.write("</locationReferences>\n")
  }

  def toteutukset(limitToteutukset: Boolean, toteutusLimit: Int): Stream[ToteutusIndexed] = {
    val rawToteutusStream = ElasticClient.listPublished(None)
    if (limitToteutukset) {
      logger.info(s"Limiting toteutus amount to $toteutusLimit as configured")
      rawToteutusStream.take(toteutusLimit)
    } else {
      rawToteutusStream
    }
  }

  def koulutustarjontaToFile(dest: BufferedWriter) = {
    val toteutusStream = toteutukset(limitToteutukset, toteutusLimit)
    lazy val koulutusStream = koulutusDependentsOfToteutukset(toteutusStream)
    lazy val tarjoajaStream = tarjoajaDependentsOfToteutukset(toteutusStream)
    dest.write("<Courses xmlns=\"http://data.europa.eu/snb/model/ap/loq-constraints/\">\n")
    toteutuksetToFile(dest, toteutusStream)
    koulutuksetToFile(dest, koulutusStream)
    tuloksetToFile(dest, koulutusStream)
    tarjoajatToFile(dest, tarjoajaStream)
    tarjoajasijainnitToFile(dest, tarjoajaStream)
    dest.write("\n</Courses>")
  }

  def koulutustarjontaAsFile(): String = {
    val tempFile = File.createTempFile("europass-export", ".xml")
    logger.info(s"publishedToteutuksetAsFile: using filename $tempFile")
    tempFile.deleteOnExit()
    val writer = new BufferedWriter(new FileWriter(tempFile))
    koulutustarjontaToFile(writer)
    writer.close()
    tempFile.getPath()
  }

}

object Publisher extends Publisher(EuropassConversion)
