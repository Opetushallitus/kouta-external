package fi.oph.kouta.europass

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.domain.indexed.{
  ToteutusIndexed,
  KoulutusIndexed,
  Organisaatio
}
import scala.xml.Elem
import java.io.{File, BufferedWriter, FileWriter}

object Publisher extends Logging {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val initialLogDelayInItems = 10
  val subsequentLogDelayInItems = 200

  def toteutusToFile(oid: String, dest: BufferedWriter) = {
    val toteutusXml = EuropassConversion.toteutusAsElmXml(ElasticClient.getToteutus(oid))
    dest.write(
      <Courses xsdVersion="3.1.0"
          xsi:schemaLocation="http://data.europa.eu/snb/model/ap/loq-constraints/loq.xsd">
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
      koulutusStream.map(EuropassConversion.koulutusAsElmXml),
      "koulutukset",
      {koulutus => dest.write(koulutus.toString)}
    )
    dest.write("</learningAchievementSpecificationReferences>\n")
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
    dest.write("<learningOpportunityReferences>\n")
    foreachWithLogging(
      toteutusXmlStream,
      "toteutukset",
      {toteutus => dest.write(toteutus.toString)}
    )
    dest.write("</learningOpportunityReferences>\n")
  }

  def tuloksetToFile(dest: BufferedWriter, koulutusStream: Stream[KoulutusIndexed]) = {
    val tulosXmlStream = koulutusStream.flatMap(EuropassConversion.koulutusTuloksetAsElmXml)
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
    .flatMap(EuropassConversion.toteutusToTarjoajaDependents)
    .toSet
    .toStream

  def tarjoajatToFile(dest: BufferedWriter, tarjoajaStream: Stream[Organisaatio]) = {
    val organisaatioXmlStream = tarjoajaStream.map(EuropassConversion.tarjoajaAsElmXml)
    dest.write("<agentReferences>\n")
    foreachWithLogging(
      organisaatioXmlStream,
      "koulutuksen tarjoajat",
      {org => dest.write(org.toString)}
    )
    dest.write("</agentReferences>\n")
  }

  def suomiLocationToFile(dest: BufferedWriter) =
    dest.write("""
      <locationReferences>
        <location id="http://rdf.oph.fi/sijainti/suomi">
          <geographicName language="fi">Suomi</geographicName>
          <geographicName language="sv">Finland</geographicName>
          <geographicName language="en">Finland</geographicName>
          <address>
            <countryCode uri="http://publications.europa.eu/resource/authority/country/FIN"/>
          </address>
        </location>
      </locationReferences>
    """)

  def koulutustarjontaToFile(dest: BufferedWriter) = {
    val toteutusStream = ElasticClient.listPublished(None)
    lazy val koulutusStream = koulutusDependentsOfToteutukset(toteutusStream)
    lazy val tarjoajaStream = tarjoajaDependentsOfToteutukset(toteutusStream)
    dest.write("<Courses xsdVersion=\"3.1.0\"\n" +
      "xsi:schemaLocation=\"http://data.europa.eu/snb/model/ap/loq-constraints/loq.xsd\">\n")
    toteutuksetToFile(dest, toteutusStream)
    koulutuksetToFile(dest, koulutusStream)
    tuloksetToFile(dest, koulutusStream)
    tarjoajatToFile(dest, tarjoajaStream)
    suomiLocationToFile(dest)
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
