package fi.oph.kouta.europass

import scala.xml._
import org.json4s._

object EuropassConversion {
  implicit val formats = DefaultFormats

  val langCodes = Map(
    "en" -> "http://publications.europa.eu/resource/authority/language/ENG",
    "fi" -> "http://publications.europa.eu/resource/authority/language/FIN",
    "sv" -> "http://publications.europa.eu/resource/authority/language/SWE"
  )

  def organisaatioUrl(oid: String): String =
    "https://rdf.oph.fi/organisaatio/" ++ oid

  def koulutusUrl(oid: String): String =
    "https://rdf.oph.fi/koulutus/" ++ oid

  def toteutusUrl(oid: String): String =
    "https://rdf.oph.fi/koulutus-toteutus/" ++ oid

  def konfoUrl(lang: String, oid: String): Elem =
    <loq:homepage
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      <loq:language uri={langCodes.getOrElse(lang, "")}/>
      <loq:contentUrl>{s"https://opintopolku.fi/konfo/$lang/toteutus/$oid"}</loq:contentUrl>
    </loq:homepage>

  def tarjoajaAsElmXml(tarjoaja: JValue): Elem =
    <loq:providedBy idref={organisaatioUrl((tarjoaja \ "oid").extract[String])}
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/"/>

  def nimiAsElmXml(lang: String, nimi: String): Elem =
    <loq:title xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/"
        language={lang}>{nimi}</loq:title>

  def toteutusAsElmXml(toteutus: JValue): Elem = {
    val oid = (toteutus \ "oid").extract[String]
    val langs = List("en", "fi", "sv")
    val titles = (toteutus \ "nimi").extract[Map[String, String]]
    <loq:learningOpportunity id={toteutusUrl(oid)}
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      {langs.map(konfoUrl(_, oid))}
      <loq:learningAchievementSpecification
          idref={koulutusUrl((toteutus \ "koulutusOid").extract[String])}/>
      {(toteutus \ "tarjoajat").children.map(tarjoajaAsElmXml)}
      {titles.keys.map(lang => nimiAsElmXml(lang, titles(lang)))}
    </loq:learningOpportunity>
  }

}
