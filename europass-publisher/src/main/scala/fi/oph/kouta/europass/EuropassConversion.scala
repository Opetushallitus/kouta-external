package fi.oph.kouta.europass

import scala.xml._
import org.json4s._
import fi.oph.kouta.external.domain.indexed.{
  KoulutusIndexed,
  ToteutusIndexed,
  TutkintoNimike,
  Organisaatio,
  AmmatillinenKoulutusMetadataIndexed,
  KorkeakoulutusKoulutusMetadataIndexed,
  ErikoislaakariKoulutusMetadataIndexed
}
import fi.oph.kouta.external.domain.Kielistetty
import fi.oph.kouta.logging.Logging

object EuropassConversion extends Logging {
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

  def tutkintoUrl(koodi: String): String =
    "https://rdf.oph.fi/tutkintonimike/" ++ koodi

  def iscedfUrl(koodi: String): String =
    "http://data.europa.eu/snb/isced-f/" ++ koodi

  def konfoUrl(lang: String, oid: String): Elem =
    <homepage>
      <language uri={langCodes.getOrElse(lang, "")}/>
      <contentUrl>{s"https://opintopolku.fi/konfo/$lang/toteutus/$oid"}</contentUrl>
    </homepage>

  def nimiAsElmXml(lang: String, nimi: String): Elem =
    <title language={lang}>{nimi}</title>

  def nimetAsElmXml(nimet: Kielistetty): List[Elem] =
    nimet.keys.map{lang => nimiAsElmXml(lang.name, nimet(lang))}.toList

  def toteutusAsElmXml(toteutus: ToteutusIndexed): Option[Elem] = {
    val oid: String = toteutus.oid.map(_.toString).getOrElse("")
    val langs = List("en", "fi", "sv")
    if (toteutus.tarjoajat.isEmpty) {
      logger.warn(s"Toteutus $oid has no tarjoajat; not publishing")
      None
    } else
    Some(<learningOpportunity id={toteutusUrl(oid)}>
      {nimetAsElmXml(toteutus.nimi)}
      {langs.map(konfoUrl(_, oid))}
      {toteutus.tarjoajat.map{t =>
        <providedBy idref={organisaatioUrl(t.oid.toString)}/>}}
      <learningAchievementSpecification
          idref={koulutusUrl((toteutus.koulutusOid.map(_.toString).getOrElse("")))}/>
      <defaultLanguage uri={langCodes.getOrElse(toteutus.kielivalinta(0).name, "")}/>
    </learningOpportunity>)
  }

  def koulutusToTutkintonimikkeet(koulutus: KoulutusIndexed): Seq[TutkintoNimike] =
    koulutus.metadata match {
      case None => Seq.empty
      case Some(md) => md match {
        case m: AmmatillinenKoulutusMetadataIndexed => m.tutkintonimike
        case m: KorkeakoulutusKoulutusMetadataIndexed => m.tutkintonimike
        case m: ErikoislaakariKoulutusMetadataIndexed => m.tutkintonimike
        case _ => Seq.empty
      }
    }

  def koulutusTuloksetAsElmXml(koulutus: KoulutusIndexed): List[Elem] = {
    val oid: String = koulutus.oid.map(_.toString).getOrElse("")
    koulutusToTutkintonimikkeet(koulutus) match {
      case empty if empty.isEmpty =>
        List(
          <learningOutcome id={tulosUrl(oid)}>
            {nimetAsElmXml(koulutus.nimi)}
          </learningOutcome>
        )
      case nonempty =>
        nonempty.map{tutkintonimike: TutkintoNimike =>
          <learningOutcome id={tutkintoUrl(tutkintonimike.koodiUri)}>
            {nimetAsElmXml(tutkintonimike.nimi)}
          </learningOutcome>
        }.toList
    }
  }

  def koulutusalaToIscedfCode(koulutusala: String): Option[String] = koulutusala match {
    case ka if ka.startsWith("kansallinenkoulutusluokitus2016koulutusalataso") =>
      // The kkl2016 and isced2013 codesets have exactly equal codes
      Some(ka.split("_")(1).split("#")(0))
    case _ => None
  }

  def iscedfAsElmXml(koodi: String) = <ISCEDFCode uri={iscedfUrl(koodi)}/>

  def koulutusAsElmXml(koulutus: KoulutusIndexed): Option[Elem] = {
    val oid: String = koulutus.oid.map(_.toString).getOrElse("")
    val iscedfCodes = koulutus.koulutuskoodienAlatJaAsteet
      .flatMap(_.koulutusalaKoodiUrit)
      .union(koulutus.metadata.map(_.koulutusala).getOrElse(List()).map(_.koodiUri))
      .flatMap(koulutusalaToIscedfCode)
      .toSet
    if (iscedfCodes.isEmpty) {
      logger.warn(s"Koulutus $oid has no koulutusala classification; not publishing")
      None
    } else
    Some(<learningAchievementSpecification id={koulutusUrl(oid)}>
      {nimetAsElmXml(koulutus.nimi)}
      {iscedfCodes.map(iscedfAsElmXml)}
      {koulutus.kielivalinta.map{lang =>
        <language uri={langCodes.getOrElse(lang.name, "")}/>}}
      {koulutusTuloksetAsElmXml(koulutus).map{tulos =>
        <learningOutcome idref={tulos \@ "id"}/>}}
    </learningAchievementSpecification>)
  }

  def toteutusToKoulutusDependents(toteutus: ToteutusIndexed): Iterable[String] =
    toteutus.koulutusOid.map(_.toString)

  def tarjoajaAsElmXml(tarjoaja: Organisaatio): Elem = {
    val nimet = tarjoaja.nimi.getOrElse(Map())
    <organisation id={organisaatioUrl(tarjoaja.oid.toString)}>
      {nimet.keys.map{lang =>
        <legalName language={lang.name}>{nimet(lang)}</legalName>}}
      <location idref="http://rdf.oph.fi/sijainti/suomi"/>
    </organisation>
  }

  def toteutusToTarjoajaDependents(
    toteutus: ToteutusIndexed
  ): List[Organisaatio] = toteutus.tarjoajat

}
