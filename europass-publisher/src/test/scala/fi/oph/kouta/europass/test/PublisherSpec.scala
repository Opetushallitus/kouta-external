package fi.oph.kouta.europass.test

import org.json4s.jackson.Serialization.{read, write}
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.io.Source
import scala.sys.process.Process
import java.io._

import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.external.domain.indexed.KoulutusIndexed
import fi.oph.kouta.europass.{Publisher, ElasticClient, ElmValidation}
import fi.oph.kouta.domain.Amm

object TestPublisher extends Publisher(TestConversion)

class PublisherSpec extends ScalatraFlatSpec with ElasticFixture with KoutaJsonFormats {

  def resource(filename: String) = Source.fromResource(filename).bufferedReader

  "toteutusToFile" should "create correct toteutusXml from ElasticSearch" in {
    val writer = new StringWriter()
    val bwriter = new BufferedWriter(writer)
    TestPublisher.toteutusToFile("1.2.246.562.17.00000000000000000001", bwriter)
    bwriter.close()
    assert(writer.toString.contains("<contentUrl>https://opintopolku.fi/konfo/sv/toteutus/1.2.246.562.17.00000000000000000001</contentUrl>"))
    assert(writer.toString.contains("<providedBy idref=\"https://rdf.oph.fi/organisaatio/1.2.246.562.10.594252633210\"/>"))

    // Want to have the test XML as a file?  Here you go:
    // val w = new BufferedWriter(new FileWriter("test.txt"))
    // w.write(writer.toString)
    // w.close()

  }

  "toteutuksetToFile" should "have toteutukset" in {
    val writer = new StringWriter()
    val bwriter = new BufferedWriter(writer)
    TestPublisher.toteutuksetToFile(bwriter, ElasticClient.listPublished(None))
    bwriter.close()
    val content = writer.toString
    assert(content.contains(
      "<learningOpportunity id=\"https://rdf.oph.fi/koulutus-toteutus/1.2.246.562.17.00000000000000000001\""
    ))
    assert(content.contains(
      "<learningOpportunity id=\"https://rdf.oph.fi/koulutus-toteutus/1.2.246.562.17.00000000000000000002\""
    ))
  }

  "tuloksetToFile" should "have information from tutkintonimike when available" in {
    val koulutusWithTutkintonimike =
      read[KoulutusIndexed](resource("koulutus-1293.json"))
    val koulutusWithoutKoulutusKoodi =
      read[KoulutusIndexed](resource("koulutus-8162.json"))
    val writer = new StringWriter()
    val bwriter = new BufferedWriter(writer)
    TestPublisher.tuloksetToFile(bwriter,
      Stream(koulutusWithTutkintonimike, koulutusWithoutKoulutusKoodi))
    bwriter.close()
    assert(writer.toString.contains(
      "<learningOutcome id=\"https://rdf.oph.fi/tutkintonimike/tutkintonimikekk_339#2\""
    ))
    assert(writer.toString.contains(
      "<learningOutcome id=\"https://rdf.oph.fi/koulutus-tulos/1.2.246.562.13.00000000000000008162\""
    ))
  }

  "koulutustarjontaToFile" should "have all kinds of objects" in {
    val writer = new StringWriter()
    val bwriter = new BufferedWriter(writer)
    TestPublisher.koulutustarjontaToFile(bwriter)
    bwriter.close()
    val content = writer.toString
    assert(content.contains(
      "<learningOpportunity id=\"https://rdf.oph.fi/koulutus-toteutus/1.2.246.562.17.00000000000000000002\""
    ))
    assert(content.contains(
      "<learningAchievementSpecification id=\"https://rdf.oph.fi/koulutus/1.2.246.562.13.00000000000000000001\""
    ))
    assert(content.contains("http://data.europa.eu/snb/isced-f/02"))
    assert(content.contains(
      "<learningOutcome id=\"https://rdf.oph.fi/tutkintonimike/tutkintonimikkeet_02\""
    ))
    assert(content.contains(
      "<organisation id=\"https://rdf.oph.fi/organisaatio/1.2.246.562.10.81934895871\""
    ))
    assert(content.contains(
      "<legalName language=\"fi\">Koulutuskeskus Salpaus</legalName>"
    ))
    assert(content.contains(
      "<geographicName language=\"sv\">Koulutuskeskus Salpaus sv</geographicName>"
    ))
    assert(content.contains(
      "<noteLiteral language=\"fi\">Polvivaara 865, 15110  LAHTI</noteLiteral>"
    ))
  }

  "koulutustarjontaAsFile" should "return valid XML file" in {
    val fileName = TestPublisher.koulutustarjontaAsFile()
    assert(fileName.contains("europass-export"))
    val content = Source.fromFile(fileName).mkString
    assert(content.contains("<title language=\"sv\">nimi sv</title>"))
    assert(ElmValidation.validateXml(fileName))
  }

  "koulutusDependentsOfToteutukset" should "have all koulutukset" in {
    val toteutukset = ElasticClient.listPublished(None)
    val koulutukset = TestPublisher.koulutusDependentsOfToteutukset(toteutukset)
    assert(koulutukset.length == 1)  // both toteutukset in example data have same koulutus
    assert(koulutukset(0).oid.map(_.toString) == Some("1.2.246.562.13.00000000000000000001"))
    assert(koulutukset(0).koulutustyyppi == Amm)
  }

}

