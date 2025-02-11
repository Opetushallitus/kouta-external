package fi.oph.kouta.europass

import scala.xml._
import org.json4s._
import fi.oph.kouta.external.domain.indexed.{KoulutusIndexed, ToteutusIndexed}

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

  def tulosUrl(oid: String): String =
    "https://rdf.oph.fi/koulutus-tulos/" ++ oid

  def iscedfUrl(koodi: String): String =
    "http://data.europa.eu/snb/isced-f/" ++ koodi

  def konfoUrl(lang: String, oid: String): Elem =
    <loq:homepage
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      <loq:language uri={langCodes.getOrElse(lang, "")}/>
      <loq:contentUrl>{s"https://opintopolku.fi/konfo/$lang/toteutus/$oid"}</loq:contentUrl>
    </loq:homepage>

  def nimiAsElmXml(lang: String, nimi: String): Elem =
    <loq:title xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/"
        language={lang}>{nimi}</loq:title>

  def toteutusAsElmXml(toteutus: ToteutusIndexed): Elem = {
    val oid: String = toteutus.oid.map(_.toString).getOrElse("")
    val langs = List("en", "fi", "sv")
    <loq:learningOpportunity id={toteutusUrl(oid)}
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      {langs.map(konfoUrl(_, oid))}
      <loq:learningAchievementSpecification
          idref={koulutusUrl((toteutus.koulutusOid.map(_.toString).getOrElse("")))}/>
      {toteutus.tarjoajat.map{t =>
        <loq:providedBy idref={organisaatioUrl(t.oid.toString)}/>}}
      {toteutus.nimi.keys.map{lang =>
        nimiAsElmXml(lang.name, toteutus.nimi(lang))}}
    </loq:learningOpportunity>
  }

  def koulutusalaToIscedfCode(koulutusala: String): Option[String] = koulutusala match {
    case ka if ka.startsWith("kansallinenkoulutusluokitus2016koulutusalataso") =>
      // The kkl2016 and isced2013 codesets have exactly equal codes
      Some(ka.split("_")(1))
    case _ => None
  }

  def iscedfAsElmXml(koodi: String) =
    <loq:ISCEDFCode uri={iscedfUrl(koodi)}
      xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/"/>

  def koulutusAsElmXml(koulutus: KoulutusIndexed): Elem = {
    val oid: String = koulutus.oid.map(_.toString).getOrElse("")
    <loq:learningAchievementSpecification id={koulutusUrl(oid)}
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      {koulutus.nimi.keys.map(lang => nimiAsElmXml(lang.name, koulutus.nimi(lang)))}
      {koulutus.koulutuskoodienAlatJaAsteet
        .flatMap(_.koulutusalaKoodiUrit)
        .flatMap(koulutusalaToIscedfCode)
        .toSet
        .map(iscedfAsElmXml)}
      {koulutus.kielivalinta.map{lang =>
        <loq:language uri={langCodes.getOrElse(lang.name, "")}/>}}
      <loq:learningOutcome idref={tulosUrl(oid)}/>
    </loq:learningAchievementSpecification>
  }

  def toteutusToKoulutusDependents(toteutus: ToteutusIndexed): Iterable[String] =
    toteutus.koulutusOid.map(_.toString)

}
