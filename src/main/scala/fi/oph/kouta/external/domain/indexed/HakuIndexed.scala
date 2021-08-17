package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.domain.{Hakulomaketyyppi, Julkaisutila, Kieli, Modified}
import fi.oph.kouta.external.domain._

import java.time.LocalDateTime
import java.util.UUID

class HakuIndexed(
    oid: Option[HakuOid],
    externalId: Option[String],
    tila: Julkaisutila,
    nimi: Kielistetty,
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
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified]
) {
  def toHaku: Haku = Haku(
    oid = oid,
    externalId = externalId,
    tila = tila,
    nimi = nimi,
    hakutapaKoodiUri = hakutapa.map(_.koodiUri),
    hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
    hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
    ajastettuJulkaisu = ajastettuJulkaisu,
    kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
    kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
    hakulomaketyyppi = hakulomaketyyppi,
    hakulomakeAtaruId = hakulomakeAtaruId,
    hakulomakeKuvaus = hakulomakeKuvaus,
    hakulomakeLinkki = hakulomakeLinkki,
    metadata = metadata.map(_.toHakuMetadata),
    organisaatioOid = organisaatio.oid,
    hakuajat = hakuajat,
    muokkaaja = muokkaaja.oid,
    kielivalinta = kielivalinta,
    modified = modified
  )
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
