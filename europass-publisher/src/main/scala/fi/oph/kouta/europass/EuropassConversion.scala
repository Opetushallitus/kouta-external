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

  def toteutusURL(oid: String): String =
    "https://rdf.oph.fi/koulutus-toteutus/" ++ oid

  def konfoUrl(lang: String, oid: String): Elem =
    <loq:homepage
      xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      <loq:language uri={langCodes.getOrElse(lang, "")}/>
      <loq:contentUrl>{s"https://opintopolku.fi/konfo/$lang/toteutus/$oid"}</loq:contentUrl>
    </loq:homepage>

  def toteutusAsElmXml(toteutus: JValue): Elem = {
    val oid = (toteutus \ "oid").extract[String]
    val langs = List("en", "fi", "sv")
    <loq:learningOpportunity id={toteutusURL(oid)}
      xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      {langs.map(konfoUrl(_, oid))}
    </loq:learningOpportunity>
  }

}
