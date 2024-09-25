package fi.oph.kouta.europass.test

import scala.io.Source
import scala.xml._
import java.io.StringWriter
import fi.oph.kouta.europass.EuropassConversion
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ConversionSpec extends ScalatraFlatSpec {
  implicit val formats = DefaultFormats
  lazy val example_toteutus: JValue = parse(
    Source.fromResource("toteutus-example-1.json").bufferedReader
  )

  "example_toteutus" should "have correct fields" in {
    assert((example_toteutus \ "koulutusOid").extract[String] == "1.2.246.562.13.00000000000000000001")
    assert((example_toteutus \ "nimi" \ "sv").extract[String] == "Glusiska haren 2022")
    assert(((example_toteutus \ "hakutiedot")(0) \ "hakuOid").extract[String] == "1.2.246.562.29.00000000000000000001")
  }

  it should "have correct namespace when converted" in {
    val toteutusXml: Elem = EuropassConversion.toteutusAsElmXml(example_toteutus)
    assert(toteutusXml.namespace == "http://data.europa.eu/snb/model/ap/loq-constraints/")
    val serialisedOutput = new StringWriter()
    XML.write(serialisedOutput, toteutusXml, "utf-8", true, null)
    val toteutusStr = serialisedOutput.toString
    assert(toteutusStr.startsWith("<?xml"))
    assert(toteutusStr.contains("loq:learningOpportunity"))
  }

  it should "contain all required elements" in {
    val toteutusXml: Elem = EuropassConversion.toteutusAsElmXml(example_toteutus)
    assert(toteutusXml \@ "id" == "https://rdf.oph.fi/koulutus-toteutus/1.2.246.562.17.00000000000000000002")
    assert(((toteutusXml \ "homepage")(0) \ "contentUrl").text == "https://opintopolku.fi/konfo/en/toteutus/1.2.246.562.17.00000000000000000002")
    assert((toteutusXml \ "homepage")(1) \ "language" \@ "uri" == "http://publications.europa.eu/resource/authority/language/FIN")
  }
}
