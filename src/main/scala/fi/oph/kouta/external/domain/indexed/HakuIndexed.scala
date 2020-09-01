package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.domain.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.external.domain._

class HakuIndexed(
    oid: Option[HakuOid],
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakutapa: Option[KoodiUri],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    alkamiskausi: Option[KoodiUri],
    alkamisvuosi: Option[String],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadata],
    organisaatio: Organisaatio,
    hakuajat: List[Ajanjakso],
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) {
  def toHaku: Haku = Haku(
    oid = oid,
    tila = tila,
    nimi = nimi,
    hakutapaKoodiUri = hakutapa.map(_.koodiUri),
    hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
    hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
    ajastettuJulkaisu = ajastettuJulkaisu,
    alkamiskausiKoodiUri = alkamiskausi.map(_.koodiUri),
    alkamisvuosi = alkamisvuosi,
    kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
    kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
    hakulomaketyyppi = hakulomaketyyppi,
    hakulomakeAtaruId = hakulomakeAtaruId,
    hakulomakeKuvaus = hakulomakeKuvaus,
    hakulomakeLinkki = hakulomakeLinkki,
    metadata = metadata,
    organisaatioOid = organisaatio.oid,
    hakuajat = hakuajat,
    muokkaaja = muokkaaja.oid,
    kielivalinta = kielivalinta,
    modified = modified
  )
}
