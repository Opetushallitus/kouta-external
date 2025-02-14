package fi.oph.kouta.europass.test

import scala.io.Source
import scala.xml._
import java.io.StringWriter
import scala.reflect.{ClassTag, classTag}

import fi.oph.kouta.europass.EuropassConversion
import fi.oph.kouta.external.util.KoutaJsonFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{read, write}
import org.scalatra.test.scalatest.ScalatraFlatSpec

import fi.oph.kouta.external.domain.indexed.{KoulutusIndexed, ToteutusIndexed}
import fi.oph.kouta.domain.{Kieli, Sv}

class ConversionSpec extends ScalatraFlatSpec with KoutaJsonFormats {

  def resource(filename: String) = Source.fromResource(filename).bufferedReader

  lazy val example_toteutus =
    read[ToteutusIndexed](resource("toteutus-example-1.json"))
  lazy val example_koulutus =
    read[KoulutusIndexed](resource("koulutus-example-1.json"))
  lazy val koulutusWithoutKoulutusKoodi =
    read[KoulutusIndexed](resource("koulutus-8162.json"))
  lazy val koulutusWithTutkintonimike =
    read[KoulutusIndexed](resource("koulutus-1293.json"))

  "example_koulutus" should "have correct fields" in {
    assert(example_koulutus.oid.getOrElse("").toString
      == "1.2.246.562.13.00000000000000000006")
    assert(example_koulutus.nimi(Sv)
      == "nimi sv")
  }

  it should "contain required elements" in {
    val koulutusXml: Elem = EuropassConversion.koulutusAsElmXml(example_koulutus)
    assert(koulutusXml \@ "id"
      == "https://rdf.oph.fi/koulutus/1.2.246.562.13.00000000000000000006")
    assert((koulutusXml \ "title")(0).text
      == "nimi fi")
    assert((koulutusXml \ "ISCEDFCode")(1) \@ "uri"
      == "http://data.europa.eu/snb/isced-f/02")
    assert((koulutusXml \ "learningOutcome")(0) \@ "idref"
      == "https://rdf.oph.fi/koulutus-tulos/1.2.246.562.13.00000000000000000006")
  }

  "example_toteutus" should "have correct fields" in {
    assert(example_toteutus.koulutusOid.getOrElse("").toString
      == "1.2.246.562.13.00000000000000000001")
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
    assert(toteutusXml \@ "id"
      == "https://rdf.oph.fi/koulutus-toteutus/1.2.246.562.17.00000000000000000002")
    assert(((toteutusXml \ "homepage")(0) \ "contentUrl").text
      == "https://opintopolku.fi/konfo/en/toteutus/1.2.246.562.17.00000000000000000002")
    assert((toteutusXml \ "homepage")(1) \ "language" \@ "uri"
      == "http://publications.europa.eu/resource/authority/language/FIN")
    assert(toteutusXml \ "learningAchievementSpecification" \@ "idref"
      == "https://rdf.oph.fi/koulutus/1.2.246.562.13.00000000000000000001")
    assert(toteutusXml \ "providedBy" \@ "idref"
      == "https://rdf.oph.fi/organisaatio/1.2.246.562.10.81934895871")
    assert((toteutusXml \ "title")(0).text
      == "Teppana jänis 2022")
    assert((toteutusXml \ "title")(1) \@ "language"
      == "sv")
  }

  it should "have correct tarjoaja" in {
    val tarjoaja: Elem =
      EuropassConversion.toteutusToTarjoajatElmXml(example_toteutus)(0)
    assert(tarjoaja \@ "id"
      == "https://rdf.oph.fi/organisaatio/1.2.246.562.10.81934895871")
    assert((tarjoaja \ "legalName")(1).text
      == "Lärocenter Salpaus")
  }

  it should "have certain koulutus as its dependent" in {
    assert(EuropassConversion.toteutusToKoulutusDependents(example_toteutus)
      == List("1.2.246.562.13.00000000000000000001"))
  }

  "koulutus 8162" should "produce koulutusala from its metadata" in {
    val koulutusXml: Elem =
      EuropassConversion.koulutusAsElmXml(koulutusWithoutKoulutusKoodi)
    assert(koulutusXml \ "ISCEDFCode" \@ "uri" ==
      "http://data.europa.eu/snb/isced-f/023")
  }

  "koulutus 1293" should "have learning outcomes from tutkintonimikkeet" in {
    val tulosXml: List[Elem] =
      EuropassConversion.koulutusTuloksetAsElmXml(koulutusWithTutkintonimike)
    assert(tulosXml(0) \@ "id"
      == "https://rdf.oph.fi/tutkintonimike/tutkintonimikekk_339#2")
    assert((tulosXml(0) \ "title")(0).text
      == "Yhteiskuntatieteiden maisteri")
    assert((tulosXml(0) \ "title")(2) \@ "language" == "sv")
  }

}
