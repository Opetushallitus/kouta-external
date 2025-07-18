package fi.oph.kouta.europass

import scala.xml._
import org.json4s._
import fi.oph.kouta.domain.{Julkaisutila, Julkaistu}
import fi.oph.kouta.external.domain.indexed.{
  KoodiUri,
  KoulutusIndexed,
  ToteutusIndexed,
  OpetusIndexed,
  TutkintoNimike,
  Organisaatio,
  AmmatillinenKoulutusMetadataIndexed,
  KorkeakoulutusKoulutusMetadataIndexed,
  ErikoislaakariKoulutusMetadataIndexed,
  KoulutuksenAlkamiskausiIndexed
}
import fi.oph.kouta.external.domain.Kielistetty
import fi.oph.kouta.logging.Logging

object EuropassConversion extends Logging {
  implicit val formats = DefaultFormats

  lazy val toteutusExtras = EuropassConfiguration.config.getBoolean(
    "europass-publisher.publishing.toteutus-extra-fields"
  )

  val langCodes = Map(
    "en" -> List("http://publications.europa.eu/resource/authority/language/ENG"),
    "fi" -> List("http://publications.europa.eu/resource/authority/language/FIN"),
    "sv" -> List("http://publications.europa.eu/resource/authority/language/SWE"),
    "oppilaitoksenopetuskieli_1" -> List("http://publications.europa.eu/resource/authority/language/FIN"),
    "oppilaitoksenopetuskieli_2" -> List("http://publications.europa.eu/resource/authority/language/SWE"),
    "oppilaitoksenopetuskieli_3" -> List(
      "http://publications.europa.eu/resource/authority/language/FIN",
      "http://publications.europa.eu/resource/authority/language/SWE"
    ),
    "oppilaitoksenopetuskieli_4" -> List("http://publications.europa.eu/resource/authority/language/ENG"),
    "oppilaitoksenopetuskieli_5" -> List("http://publications.europa.eu/resource/authority/language/SME")
  )

  def organisaatioUrl(oid: String): String =
    "https://rdf.oph.fi/organisaatio/" ++ oid

  def sijaintiUrl(oid: String): String =
    "https://rdf.oph.fi/organisaatio-sijainti/" ++ oid

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
      <language uri={langCodes.getOrElse(lang, List(""))(0)}/>
      <contentUrl>{s"https://opintopolku.fi/konfo/$lang/toteutus/$oid"}</contentUrl>
    </homepage>

  def nimiAsElmXml(lang: String, nimi: String): Elem =
    <title language={lang}>{nimi}</title>

  def nimetAsElmXml(nimet: Kielistetty): List[Elem] =
    nimet.keys.map{lang => nimiAsElmXml(lang.name, nimet(lang))}.toList

  def toteutusOpetuskieliAsElmCode(toteutus: ToteutusIndexed): String = {
    val opetuskieliKoodit: Seq[String] =
      toteutus.metadata
        .flatMap(_.opetus)
        .map(_.opetuskieli)
        .getOrElse(List())
        .map{koodiobj => koodiobj.koodiUri.split("#")(0)}
        .flatMap{koodi => langCodes.getOrElse(koodi, List())}
    val kielivalintaKoodit: Seq[String] =
      toteutus.kielivalinta.map(_.name)
        .flatMap{koodi => langCodes.getOrElse(koodi, List())}
    val default = "http://publications.europa.eu/resource/authority/language/OP_DATPRO"
    if (opetuskieliKoodit.nonEmpty)
      opetuskieliKoodit(0)
    else if (kielivalintaKoodit.nonEmpty)
      kielivalintaKoodit(0)
    else default
  }

  def toteutuksenKuvausAsElmXml(toteutus: ToteutusIndexed): Seq[Elem] =
    toteutus.metadata.map(_.kuvaus) match {
      case Some(kuvaukset: Kielistetty) if toteutusExtras =>
        kuvaukset.keys.map{lang =>
          <description language={lang.name}>{kuvaukset(lang)}</description>
        }.toList
      case _ => List()
    }

  def approximateStartDate(kausi: Option[KoodiUri], vuosi: Option[String]): Option[String] =
    (kausi, vuosi) match {
      case (Some(KoodiUri("kausi_s#1")), Some(vuosi)) => Some(s"$vuosi-08-01T00:00")
      case (Some(KoodiUri("kausi_k#1")), Some(vuosi)) => Some(s"$vuosi-01-01T00:00")
      case _ => None
    }

  def toteutuksenAjankohtaAsElmXml(toteutus: ToteutusIndexed): Seq[Elem] =
    toteutus.metadata.flatMap(_.opetus).flatMap(_.koulutuksenAlkamiskausi) match {
      case Some(kausi: KoulutuksenAlkamiskausiIndexed) if toteutusExtras =>
        List(<temporal>
          {kausi.koulutuksenAlkamispaivamaara.orElse(
            approximateStartDate(kausi.koulutuksenAlkamiskausi, kausi.koulutuksenAlkamisvuosi)
          ).map{alku => <startDate>{alku}:00</startDate>}.toList}
          {kausi.koulutuksenPaattymispaivamaara.map{loppu => <endDate>{loppu}:00</endDate>}.toList}
        </temporal>)
      case _ => List()
    }

  def toteutuksenKestoAsElmXml(toteutus: ToteutusIndexed): Seq[Elem] = {
    val opetus: Option[OpetusIndexed] = toteutus.metadata.flatMap(_.opetus)
    val duration: String =
      opetus.flatMap(_.suunniteltuKestoVuodet).map(_ + "Y").getOrElse("") +
      opetus.flatMap(_.suunniteltuKestoKuukaudet).map(_ + "M").getOrElse("")
    if (toteutusExtras && duration.nonEmpty)
      List(<duration>{"P" + duration}</duration>)
    else List()
  }

  def toteutuksenAikatauluAsElmXml(toteutus: ToteutusIndexed): Seq[Elem] = {
    val opetus: Option[OpetusIndexed] = toteutus.metadata.flatMap(_.opetus)
    (opetus.map(_.opetusaikaKuvaus).map(_.toList).getOrElse(List()) ++
      opetus.map(_.opetustapaKuvaus).map(_.toList).getOrElse(List()))
        .take(1)  // No way to express language alternatives so we just pick a random one
        .map{case (lang, desc) =>
          <scheduleInformation>
            <noteLiteral language={lang.name}>{desc}</noteLiteral>
          </scheduleInformation>
        }.toList
  }

  def toteutusAsElmXml(toteutus: ToteutusIndexed): Option[Elem] = {
    val oid: String = toteutus.oid.map(_.toString).getOrElse("")
    val langs = List("en", "fi", "sv")
    if (toteutus.tila != Julkaistu) {
      logger.warn(s"Toteutus $oid is in state ${toteutus.tila}; not publishing")
      None
    } else if (toteutus.tarjoajat.isEmpty) {
      logger.warn(s"Toteutus $oid has no tarjoajat; not publishing")
      None
    } else
    Some(<learningOpportunity id={toteutusUrl(oid)}>
      {nimetAsElmXml(toteutus.nimi)}
      {toteutuksenKuvausAsElmXml(toteutus)}
      {langs.map(konfoUrl(_, oid))}
      {toteutuksenAjankohtaAsElmXml(toteutus)}
      {toteutuksenKestoAsElmXml(toteutus)}
      {toteutuksenAikatauluAsElmXml(toteutus)}
      {toteutus.tarjoajat.map{t =>
        <providedBy idref={organisaatioUrl(t.oid.toString)}/>}}
      <learningAchievementSpecification
          idref={koulutusUrl((toteutus.koulutusOid.map(_.toString).getOrElse("")))}/>
      <defaultLanguage uri={toteutusOpetuskieliAsElmCode(toteutus)}/>
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

  val moniala_re = "kansallinenkoulutusluokitus2016koulutusalataso3_(..)[12]8".r

  def koulutusalaToIscedfCode(koulutusala: String): Option[String] = koulutusala match {
    case moniala_re(level2Category) =>
      // These codes are apparently OPH "monialainen koulutus" additions to ISCED-F 2013
      Some(level2Category + "88")  // Inter-disciplinary programmes
    case ka if ka.startsWith("kansallinenkoulutusluokitus2016koulutusalataso3_0820") =>
      // This code is a weird OPH addition (generic forestry)
      None
    case ka if ka.startsWith("kansallinenkoulutusluokitus2016koulutusalataso") =>
      // The kkl2016 and isced2013 codesets have exactly equal codes
      Some(ka.split("_")(1).split("#")(0))
    case _ => None
  }

  def iscedfAsElmXml(koodi: String) = <ISCEDFCode uri={iscedfUrl(koodi)}/>

  def koulutusAsElmXml(koulutus: KoulutusIndexed): Option[Elem] = {
    val oid: String = koulutus.oid.map(_.toString).getOrElse("")
    val iscedfCodes = Some(
        koulutus.koulutuskoodienAlatJaAsteet
        .flatMap(_.koulutusalaKoodiUrit)
        .union(koulutus.metadata.map(_.koulutusala).getOrElse(List()).map(_.koodiUri))
        .flatMap(koulutusalaToIscedfCode)
        .toSet)
      .filter(_.nonEmpty)
      .getOrElse(List("0099"))  // generic prog and qfic not elsewhere classified
    Some(<learningAchievementSpecification id={koulutusUrl(oid)}>
      {nimetAsElmXml(koulutus.nimi)}
      {iscedfCodes.map(iscedfAsElmXml)}
      {koulutus.kielivalinta.map{lang =>
        <language uri={langCodes.getOrElse(lang.name, List(""))(0)}/>}}
      {koulutusTuloksetAsElmXml(koulutus).map{tulos =>
        <learningOutcome idref={tulos \@ "id"}/>}}
    </learningAchievementSpecification>)
  }

  def toteutusToKoulutusDependents(toteutus: ToteutusIndexed): Iterable[String] =
    toteutus.koulutusOid.map(_.toString)

  def tarjoajaAsElmXml(tarjoaja: Organisaatio): Elem = {
    val nimet = tarjoaja.nimi.getOrElse(Map())
    val oid = tarjoaja.oid.toString
    <organisation id={organisaatioUrl(oid)}>
      {nimet.keys.map{lang =>
        <legalName language={lang.name}>{nimet(lang)}</legalName>}}
      <location idref={sijaintiUrl(oid)}/>
    </organisation>
  }

  def tarjoajasijaintiAsElmXml(tarjoaja: Organisaatio): Elem = {
    val nimet = tarjoaja.nimi.getOrElse(Map())
    val oid = tarjoaja.oid.toString
    <location id={sijaintiUrl(oid)}>
      {nimet.keys.map{lang =>
        <geographicName language={lang.name}>{nimet(lang)}</geographicName>}}
      <address>
        {OrganisationClient.getOrganisationAddress(oid).map{addr =>
          <fullAddress><noteLiteral language="fi">{addr}</noteLiteral></fullAddress>
        }.getOrElse(List())}
        <countryCode uri="http://publications.europa.eu/resource/authority/country/FIN"/>
      </address>
    </location>
  }

  def toteutusToTarjoajaDependents(
    toteutus: ToteutusIndexed
  ): List[Organisaatio] = toteutus.tarjoajat

}
