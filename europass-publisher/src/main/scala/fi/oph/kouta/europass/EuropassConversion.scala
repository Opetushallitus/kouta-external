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

  def tutkintoUrl(koodi: String): String =
    "https://rdf.oph.fi/tutkintonimike/" ++ koodi

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

  def nimetAsElmXml(nimet: Kielistetty): List[Elem] =
    nimet.keys.map{lang => nimiAsElmXml(lang.name, nimet(lang))}.toList

  def toteutusAsElmXml(toteutus: ToteutusIndexed): Elem = {
    val oid: String = toteutus.oid.map(_.toString).getOrElse("")
    val langs = List("en", "fi", "sv")
    <loq:learningOpportunity id={toteutusUrl(oid)}
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      {nimetAsElmXml(toteutus.nimi)}
      {langs.map(konfoUrl(_, oid))}
      {toteutus.tarjoajat.map{t =>
        <loq:providedBy idref={organisaatioUrl(t.oid.toString)}/>}}
      <loq:learningAchievementSpecification
          idref={koulutusUrl((toteutus.koulutusOid.map(_.toString).getOrElse("")))}/>
      <loq:defaultLanguage uri={langCodes.getOrElse(toteutus.kielivalinta(0).name, "")}/>
    </loq:learningOpportunity>
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
          <loq:learningOutcome id={tulosUrl(oid)}
              xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
            {nimetAsElmXml(koulutus.nimi)}
          </loq:learningOutcome>
        )
      case nonempty =>
        nonempty.map{tutkintonimike: TutkintoNimike =>
          <loq:learningOutcome id={tutkintoUrl(tutkintonimike.koodiUri)}
              xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
            {nimetAsElmXml(tutkintonimike.nimi)}
          </loq:learningOutcome>
        }.toList
    }
  }

  def koulutusalaToIscedfCode(koulutusala: String): Option[String] = koulutusala match {
    case ka if ka.startsWith("kansallinenkoulutusluokitus2016koulutusalataso") =>
      // The kkl2016 and isced2013 codesets have exactly equal codes
      Some(ka.split("_")(1).split("#")(0))
    case _ => None
  }

  def iscedfAsElmXml(koodi: String) =
    <loq:ISCEDFCode uri={iscedfUrl(koodi)}
      xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/"/>

  def koulutusAsElmXml(koulutus: KoulutusIndexed): Elem = {
    val oid: String = koulutus.oid.map(_.toString).getOrElse("")
    <loq:learningAchievementSpecification id={koulutusUrl(oid)}
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      {nimetAsElmXml(koulutus.nimi)}
      {koulutus.koulutuskoodienAlatJaAsteet
        .flatMap(_.koulutusalaKoodiUrit)
        .union(koulutus.metadata.map(_.koulutusala).getOrElse(List()).map(_.koodiUri))
        .flatMap(koulutusalaToIscedfCode)
        .toSet
        .map(iscedfAsElmXml)}
      {koulutus.kielivalinta.map{lang =>
        <loq:language uri={langCodes.getOrElse(lang.name, "")}/>}}
      {koulutusTuloksetAsElmXml(koulutus).map{tulos =>
        <loq:learningOutcome idref={tulos \@ "id"}/>}}
    </loq:learningAchievementSpecification>
  }

  def toteutusToKoulutusDependents(toteutus: ToteutusIndexed): Iterable[String] =
    toteutus.koulutusOid.map(_.toString)

  def tarjoajaAsElmXml(tarjoaja: Organisaatio): Elem = {
    val nimet = tarjoaja.nimi.getOrElse(Map())
    <loq:organisation id={organisaatioUrl(tarjoaja.oid.toString)}
        xmlns:loq="http://data.europa.eu/snb/model/ap/loq-constraints/">
      {nimet.keys.map{lang =>
        <loq:legalName language={lang.name}>{nimet(lang)}</loq:legalName>}}
      <loq:location idref="http://rdf.oph.fi/sijainti/suomi"/>
    </loq:organisation>
  }

  def toteutusToTarjoajaDependents(
    toteutus: ToteutusIndexed
  ): List[Organisaatio] = toteutus.tarjoajat

}
