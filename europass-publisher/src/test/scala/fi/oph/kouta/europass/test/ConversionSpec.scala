package fi.oph.kouta.europass.test

import scala.io.Source
import scala.xml._
import java.io.StringWriter
import scala.reflect.{ClassTag, classTag}

import fi.oph.kouta.europass.EuropassConversion
import fi.oph.kouta.external.util.KoutaJsonFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.scalatra.test.scalatest.ScalatraFlatSpec
import com.fasterxml.jackson.databind.{ObjectMapper, DeserializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import fi.oph.kouta.external.domain.indexed.ToteutusIndexed
import fi.oph.kouta.domain.{Kieli, Sv}

class ConversionSpec extends ScalatraFlatSpec with KoutaJsonFormats {
  lazy val example_toteutus_jvalue: JValue = parse(
    Source.fromResource("toteutus-example-1.json").bufferedReader
  )
  lazy val example_toteutus: ToteutusIndexed = example_toteutus_jvalue.extract[ToteutusIndexed]

  "example_toteutus" should "have correct fields" in {
    assert(example_toteutus.koulutusOid.getOrElse("").toString == "1.2.246.562.13.00000000000000000001")
    assert(example_toteutus.nimi(Sv) == "Glusiska haren 2022")
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
    assert(toteutusXml \ "learningAchievementSpecification" \@ "idref" == "https://rdf.oph.fi/koulutus/1.2.246.562.13.00000000000000000001")
    assert(toteutusXml \ "providedBy" \@ "idref" == "https://rdf.oph.fi/organisaatio/1.2.246.562.10.81934895871")
    assert((toteutusXml \ "title")(0).text == "Teppana jänis 2022")
    assert((toteutusXml \ "title")(1) \@ "language" == "sv")
  }
}
