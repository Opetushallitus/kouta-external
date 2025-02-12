package fi.oph.kouta.europass

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.domain.indexed.{ToteutusIndexed, KoulutusIndexed}
import scala.xml.Elem
import java.io.{File, BufferedWriter, FileWriter}

object Publisher extends Logging {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val initialLogDelayInItems = 10
  val subsequentLogDelayInItems = 200

  def toteutusToFile(oid: String, dest: BufferedWriter) = {
    val toteutusXml = EuropassConversion.toteutusAsElmXml(ElasticClient.getToteutus(oid))
    dest.write(
      <loq:Courses xsdVersion="3.1.0"
          xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
        <loq:learningOpportunityReferences>
          {toteutusXml}
        </loq:learningOpportunityReferences>
      </loq:Courses>.toString
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
    dest.write("<loq:learningAchievementSpecificationReferences>\n")
    foreachWithLogging(
      koulutusStream.map(EuropassConversion.koulutusAsElmXml),
      "koulutukset",
      {koulutus => dest.write(koulutus.toString)}
    )
    dest.write("</loq:learningAchievementSpecificationReferences>\n")
  }

  def koulutusDependentsOfToteutukset(
    toteutusStream: Stream[ToteutusIndexed]
  ): Stream[KoulutusIndexed] =
    toteutusStream
      .flatMap(EuropassConversion.toteutusToKoulutusDependents)
      .toSet
      .toStream
      .map(ElasticClient.getKoulutus)

  def toteutuksetToFile(dest: BufferedWriter, toteutusStream: Stream[ToteutusIndexed]) = {
    val toteutusXmlStream = toteutusStream.map(EuropassConversion.toteutusAsElmXml)
    dest.write("<loq:learningOpportunityReferences>\n")
    foreachWithLogging(
      toteutusXmlStream,
      "toteutukset",
      {toteutus => dest.write(toteutus.toString)}
    )
    dest.write("</loq:learningOpportunityReferences>\n")
  }

  def koulutustarjontaToFile(dest: BufferedWriter) = {
    val toteutusStream = ElasticClient.listPublished(None)
    dest.write("<loq:Courses xsdVersion=\"3.1.0\"\n" +
      "xmlns:loq=\"http://data.europa.eu/snb/model/ap/loq-constraints/\">\n")
    toteutuksetToFile(dest, toteutusStream)
    koulutuksetToFile(dest, koulutusDependentsOfToteutukset(toteutusStream))
    dest.write("\n</loq:Courses>")
    // dest.close()
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
