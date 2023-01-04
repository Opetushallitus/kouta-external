package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid}
import fi.oph.kouta.domain.{Hakulomaketyyppi, Julkaisutila, Kieli, Modified}
import fi.oph.kouta.external.domain._
import fi.vm.sade.utils.slf4j.Logging

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

case class EmbeddedToteutusIndexed(tarjoajat: List[Organisaatio])
case class EmbeddedHakukohdeIndexed(
    oid: HakukohdeOid,
    jarjestyspaikka: Option[Organisaatio],
    toteutus: EmbeddedToteutusIndexed,
    tila: Julkaisutila
)
case class HakuIndexed(
    oid: Option[HakuOid],
    externalId: Option[String],
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakukohteet: List[EmbeddedHakukohdeIndexed],
    hakutapa: Option[KoodiUri],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadataIndexed],
    organisaatio: Organisaatio,
    hakuajat: List[Ajanjakso],
    valintakokeet: Option[List[ValintakoeIndexed]],
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified]
) extends WithTila
    with Logging {
  def toHaku(includeHakukohdeOids: Boolean = false): Haku = {
    def getHakukausiUri(ajanjakso: Ajanjakso): String = {
      ajanjakso.paattyy.map(_.getMonthValue).getOrElse {
        ajanjakso.alkaa.getMonthValue
      }
    } match {
      case m if m >= 1 && m <= 7  => "kausi_k#1"
      case m if m >= 8 && m <= 12 => "kausi_s#1"
      case _                      => ""
    }

    try {
      Haku(
        oid = oid,
        hakukohdeOids = if (includeHakukohdeOids) Some(hakukohteet.map(_.oid)) else None,
        tila = tila,
        nimi = nimi,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
        hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
        ajastettuJulkaisu = ajastettuJulkaisu,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.toHakuMetadata.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausiKoodiUri)
        ),
        hakuvuosi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption
          .map(ha => ha.paattyy.map(_.getYear).getOrElse(ha.alkaa.getYear)),
        hakukausi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption
          .map(getHakukausiUri),
        alkamisvuosi =
          metadata.flatMap(m => m.toHakuMetadata.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
        kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        metadata = metadata.map(_.toHakuMetadata),
        organisaatioOid = organisaatio.oid,
        hakuajat = hakuajat,
        valintakokeet = valintakokeet.map(vl => vl.map(_.toValintakoe)),
        muokkaaja = muokkaaja.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Haku (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }
  implicit val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))

}

class HakuMetadataIndexed(
    yhteyshenkilot: Seq[Yhteyshenkilo],
    tulevaisuudenAikataulu: Seq[Ajanjakso],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiIndexed]
) {
  def toHakuMetadata: HakuMetadata = HakuMetadata(
    yhteyshenkilot = yhteyshenkilot,
    tulevaisuudenAikataulu = tulevaisuudenAikataulu,
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.map(_.toKoulutuksenAlkamiskausi)
  )
}
