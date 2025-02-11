package fi.oph.kouta.europass.test

import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.io.Source
import java.io._

import fi.oph.kouta.europass.Publisher
import fi.oph.kouta.europass.ElasticClient
import fi.oph.kouta.domain.Amm

class PublisherSpec extends ScalatraFlatSpec with ElasticFixture {

  "toteutusToFile" should "create correct toteutusXml from ElasticSearch" in {
    val writer = new StringWriter()
    val bwriter = new BufferedWriter(writer)
    Publisher.toteutusToFile("1.2.246.562.17.00000000000000000001", bwriter)
    bwriter.close()
    assert(writer.toString.contains("<loq:contentUrl>https://opintopolku.fi/konfo/sv/toteutus/1.2.246.562.17.00000000000000000001</loq:contentUrl>"))
    assert(writer.toString.contains("<loq:providedBy idref=\"https://rdf.oph.fi/organisaatio/1.2.246.562.10.594252633210\"/>"))

    // Want to have the test XML as a file?  Here you go:
    // val w = new BufferedWriter(new FileWriter("test.txt"))
    // w.write(writer.toString)
    // w.close()

  }

  "toteutuksetToFile" should "create XML from ElasticSearch" in {
    val writer = new StringWriter()
    val bwriter = new BufferedWriter(writer)
    Publisher.toteutuksetToFile(bwriter, ElasticClient.listPublished(None))
    bwriter.close()
    val content = writer.toString
    assert(content.contains(
      "<loq:learningOpportunity id=\"https://rdf.oph.fi/koulutus-toteutus/1.2.246.562.17.00000000000000000001\""
    ))
    assert(content.contains(
      "<loq:learningOpportunity id=\"https://rdf.oph.fi/koulutus-toteutus/1.2.246.562.17.00000000000000000002\""
    ))
  }

  "publishedToteutuksetAsFile" should "return XML filename" in {
    val fileName = Publisher.koulutustarjontaAsFile()
    assert(fileName.contains("europass-export"))
    val content = Source.fromFile(fileName).mkString
    assert(content.contains("<loq:title language=\"sv\">nimi sv</loq:title>"))
  }

  "koulutusDependentsOfToteutukset" should "have all koulutukset" in {
    val toteutukset = ElasticClient.listPublished(None)
    val koulutukset = Publisher.koulutusDependentsOfToteutukset(toteutukset)
    assert(koulutukset.length == 1)  // both toteutukset in example data have same koulutus
    assert(koulutukset(0).oid.map(_.toString) == Some("1.2.246.562.13.00000000000000000001"))
    assert(koulutukset(0).koulutustyyppi == Amm)
  }

}

