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

object TestConversion extends EuropassConversion(TestOrganisationClient)

class ConversionSpec extends ScalatraFlatSpec with KoutaJsonFormats {

  def resource(filename: String) = Source.fromResource(filename).bufferedReader

  lazy val example_toteutus =
    read[ToteutusIndexed](resource("toteutus-example-1.json"))
  lazy val toteutusArchived =
    read[ToteutusIndexed](resource("toteutus-arkistoitu.json"))
  lazy val toteutusWithoutTarjoajat =
    read[ToteutusIndexed](resource("toteutus-ei-tarjoajia.json"))
  lazy val toteutusWithoutOpetuskieli =
    read[ToteutusIndexed](resource("toteutus-ei-opetuskielta.json"))
  lazy val toteutusTotallyOrdinary =
    read[ToteutusIndexed](resource("toteutus-ihan-tavanomainen.json"))
  lazy val toteutusExactStartTime =
    read[ToteutusIndexed](resource("toteutus-tarkka-alkuaika.json"))

  lazy val example_koulutus =
    read[KoulutusIndexed](resource("koulutus-example-1.json"))
  lazy val koulutusWithoutKoulutusKoodi =
    read[KoulutusIndexed](resource("koulutus-8162.json"))
  lazy val koulutusWithoutKoulutusAla =
    read[KoulutusIndexed](resource("koulutus-ilman-koulutusalaa.json"))
  lazy val koulutusWithMultidisciplinaryKoulutusAla =
    read[KoulutusIndexed](resource("koulutus-1806.json"))
  lazy val koulutusWith0820 =
    read[KoulutusIndexed](resource("koulutus-2032.json"))
  lazy val koulutusWithTutkintonimike =
    read[KoulutusIndexed](resource("koulutus-1293.json"))

  "example_koulutus" should "have correct fields" in {
    assert(example_koulutus.oid.getOrElse("").toString
      == "1.2.246.562.13.00000000000000000006")
    assert(example_koulutus.nimi(Sv)
      == "nimi sv")
  }

  it should "contain required elements" in {
    val Some(koulutusXml: Elem) = TestConversion.koulutusAsElmXml(example_koulutus)
    assert(koulutusXml \@ "id"
      == "https://rdf.oph.fi/koulutus/1.2.246.562.13.00000000000000000006")
    assert((koulutusXml \ "title")(0).text
      == "nimi fi")
    assert((koulutusXml \ "ISCEDFCode")(1) \@ "uri"
      == "http://data.europa.eu/snb/isced-f/02")
    assert((koulutusXml \ "learningOutcome")(0) \@ "idref"
      == "https://rdf.oph.fi/koulutus-tulos/1.2.246.562.13.00000000000000000006")
  }

  "totally ordinary toteutus" should "have all optional fields" in {
    val Some(toteutusXml: Elem) = TestConversion.toteutusAsElmXml(toteutusTotallyOrdinary)
    assert(toteutusXml \ "description" \@ "language" == "en")
    assert((toteutusXml \ "duration").text == "P0Y0M")
    // from opetustapaKuvaus, since it doesn't have opetusaikaKuvaus
    assert(toteutusXml \ "scheduleInformation" \ "noteLiteral" \@ "language" == "en")
    assert((toteutusXml \ "scheduleInformation" \ "noteLiteral").text
      .startsWith("<p>Online course."))
  }

  "example_toteutus" should "have correct fields" in {
    assert(example_toteutus.koulutusOid.getOrElse("").toString
      == "1.2.246.562.13.00000000000000000001")
    assert(example_toteutus.nimi(Sv) == "Glusiska haren 2022")
  }

  it should "have correct namespace when converted" in {
    val Some(toteutusXml: Elem) = TestConversion.toteutusAsElmXml(example_toteutus)
    assert(toteutusXml.namespace == null)
    val serialisedOutput = new StringWriter()
    XML.write(serialisedOutput, toteutusXml, "utf-8", true, null)
    val toteutusStr = serialisedOutput.toString
    assert(toteutusStr.startsWith("<?xml"))
    assert(toteutusStr.contains("learningOpportunity"))
  }

  it should "contain all required elements" in {
    val Some(toteutusXml: Elem) = TestConversion.toteutusAsElmXml(example_toteutus)
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
    assert(toteutusXml \ "defaultLanguage" \@ "uri"
      == "http://publications.europa.eu/resource/authority/language/FIN")
  }

  it should "have optional elements" in {
    val Some(toteutusXml: Elem) = TestConversion.toteutusAsElmXml(example_toteutus)
    assert((toteutusXml \ "temporal" \ "startDate").text == "2023-01-01T00:00:00")
    assert((toteutusXml \ "temporal" \ "endDate").toList == List())
    assert((toteutusXml \ "duration").text == "P3Y10M")
    assert((toteutusXml \ "scheduleInformation" \ "noteLiteral").text
      == "Opetusaikakuvaus fi")
  }

  it should "have correct tarjoaja" in {
    val tarjoaja: Elem =
      TestConversion.toteutusToTarjoajaDependents(example_toteutus)
      .map(TestConversion.tarjoajaAsElmXml)
      .head
    assert(tarjoaja \@ "id"
      == "https://rdf.oph.fi/organisaatio/1.2.246.562.10.81934895871")
    assert((tarjoaja \ "legalName")(1).text
      == "Lärocenter Salpaus")
  }

  it should "have certain koulutus as its dependent" in {
    assert(TestConversion.toteutusToKoulutusDependents(example_toteutus)
      == List("1.2.246.562.13.00000000000000000001"))
  }

  "toteutus with exact start time" should "have exact start time" in {
    val Some(toteutusXml: Elem) = TestConversion.toteutusAsElmXml(toteutusExactStartTime)
    assert((toteutusXml \ "temporal" \ "startDate").text == "2021-08-04T12:00:00")
    assert((toteutusXml \ "temporal" \ "endDate").text == "2022-12-22T00:00:00")
  }

  "toteutus without tarjoajat" should "not procude an ELM record" in {
    assert(TestConversion.toteutusAsElmXml(toteutusWithoutTarjoajat) == None)
  }

  "toteutus in non-julkaistu state" should "not produce ELM record" in {
    assert(TestConversion.toteutusAsElmXml(toteutusArchived) == None)
  }

  "toteutus without opetuskieli" should "produce defaultLanguage from kielivalinta" in {
    val Some(toteutusXml: Elem) =
      TestConversion.toteutusAsElmXml(toteutusWithoutOpetuskieli)
    assert(toteutusXml \ "defaultLanguage" \@ "uri" ==
      "http://publications.europa.eu/resource/authority/language/ENG")
  }

  "koulutus without koulutusalakoodit" should
  "produce koulutusala from its metadata" in {
    val Some(koulutusXml: Elem) =
      TestConversion.koulutusAsElmXml(koulutusWithoutKoulutusKoodi)
    assert(koulutusXml \ "ISCEDFCode" \@ "uri" ==
      "http://data.europa.eu/snb/isced-f/023")
  }

  "koulutus without koulutusala and tutkintonimike" should
  "include generic ISCED-F" in {
    val Some(koulutusXml: Elem) =
      TestConversion.koulutusAsElmXml(koulutusWithoutKoulutusAla)
    assert(koulutusXml \ "ISCEDFCode" \@ "uri" ==
      "http://data.europa.eu/snb/isced-f/0099")
  }

  it should "not have tutkintonimike based learning outcome" in {
    val Some(koulutusXml: Elem) =
      TestConversion.koulutusAsElmXml(koulutusWithoutKoulutusAla)
    assert(koulutusXml \ "learningOutcome" \@ "idref" ==
      "https://rdf.oph.fi/koulutus-tulos/1.2.246.562.13.00000000000000008751")
  }

  "koulutus with tutkintonimike and multidisciplinary koulutusala" should
  "translate multidisciplinary into ISCED-F" in {
    val Some(koulutusXml: Elem) =
      TestConversion.koulutusAsElmXml(koulutusWithMultidisciplinaryKoulutusAla)
    assert((koulutusXml \ "ISCEDFCode").map{_ \@ "uri"} ==
      List(
        "http://data.europa.eu/snb/isced-f/09",
        "http://data.europa.eu/snb/isced-f/091",
        "http://data.europa.eu/snb/isced-f/0988"
      ))
  }

  it should "have tutkintonimike based learning outcome" in {
    val Some(koulutusXml: Elem) =
      TestConversion.koulutusAsElmXml(koulutusWithMultidisciplinaryKoulutusAla)
    assert(koulutusXml \ "learningOutcome" \@ "idref" ==
      "https://rdf.oph.fi/tutkintonimike/tutkintonimikekk_714#2")
  }

  "koulutus with finland-only koulutusala 0820" should
  "not have non-existent ISCED-F codes" in {
    val Some(koulutusXml: Elem) =
      TestConversion.koulutusAsElmXml(koulutusWith0820)
    assert((koulutusXml \ "ISCEDFCode").map{_ \@ "uri"} ==
      List(
        "http://data.europa.eu/snb/isced-f/08",
        "http://data.europa.eu/snb/isced-f/082",
        "http://data.europa.eu/snb/isced-f/0821"
      ))
  }

  "koulutus with tutkintonimike" should
  "have learning outcomes from tutkintonimikkeet" in {
    val tulosXml: List[Elem] =
      TestConversion.koulutusTuloksetAsElmXml(koulutusWithTutkintonimike)
    assert(tulosXml(0) \@ "id"
      == "https://rdf.oph.fi/tutkintonimike/tutkintonimikekk_339#2")
    assert((tulosXml(0) \ "title")(0).text
      == "Yhteiskuntatieteiden maisteri")
    assert((tulosXml(0) \ "title")(2) \@ "language" == "sv")
  }

}
