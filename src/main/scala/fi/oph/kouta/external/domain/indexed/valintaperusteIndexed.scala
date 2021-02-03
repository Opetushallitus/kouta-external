package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.external.domain._
import fi.oph.kouta.domain.{Amm, Julkaisutila, Kieli, Koulutustyyppi}

case class ValintaperusteIndexed(
    id: Option[UUID],
    tila: Julkaisutila,
    koulutustyyppi: Koulutustyyppi,
    hakutapa: Option[KoodiUri],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    nimi: Kielistetty,
    julkinen: Boolean,
    sorakuvaus: Option[UuidObject],
    valintakokeet: List[ValintakoeIndexed],
    metadata: Option[ValintaperusteMetadataIndexed],
    organisaatio: Organisaatio,
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) {
  def toValintaperuste: Valintaperuste = Valintaperuste(
    id = id,
    koulutustyyppi = koulutustyyppi,
    tila = tila,
    hakutapaKoodiUri = hakutapa.map(_.koodiUri),
    kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
    kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
    nimi = nimi,
    julkinen = julkinen,
    sorakuvausId = sorakuvaus.map(_.id),
    valintakokeet = valintakokeet.map(_.toValintakoe),
    metadata = metadata.map(_.toValintaperusteMetadata),
    organisaatioOid = organisaatio.oid,
    muokkaaja = muokkaaja.oid,
    kielivalinta = kielivalinta,
    modified = modified
  )
}

sealed trait ValintaperusteMetadataIndexed {
  def tyyppi: Koulutustyyppi
  def valintatavat: Seq[ValintatapaIndexed]
  def kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed]
  def kuvaus: Kielistetty

  def toValintaperusteMetadata: ValintaperusteMetadata
}

case class AmmatillinenValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Amm,
    valintatavat: Seq[ValintatapaIndexed],
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed],
    kuvaus: Kielistetty
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AmmatillinenValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kielitaitovaatimukset = kielitaitovaatimukset.map(_.toValintaperusteKielitaitovaatimus),
      kuvaus = kuvaus
    )
}

sealed trait KorkeakoulutusValintaperusteMetadataIndexed extends ValintaperusteMetadataIndexed {
  def osaamistausta: Seq[KoodiUri]
}

case class YliopistoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed],
    osaamistausta: Seq[KoodiUri],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: YliopistoValintaperusteMetadata = YliopistoValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toValintatapa),
    kielitaitovaatimukset = kielitaitovaatimukset.map(_.toValintaperusteKielitaitovaatimus),
    osaamistaustaKoodiUrit = osaamistausta.map(_.koodiUri),
    kuvaus = kuvaus
  )
}

case class AmmattikorkeakouluValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed],
    osaamistausta: Seq[KoodiUri],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: AmmattikorkeakouluValintaperusteMetadata =
    AmmattikorkeakouluValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kielitaitovaatimukset = kielitaitovaatimukset.map(_.toValintaperusteKielitaitovaatimus),
      osaamistaustaKoodiUrit = osaamistausta.map(_.koodiUri),
      kuvaus = kuvaus
    )
}

case class ValintaperusteKielitaitovaatimusIndexed(
    kieli: Option[KoodiUri],
    kielitaidonVoiOsoittaa: Seq[KielitaidonVoiOsoittaaIndexed],
    vaatimukset: Seq[KielitaitovaatimusIndexed]
) {
  def toValintaperusteKielitaitovaatimus: ValintaperusteKielitaitovaatimus = ValintaperusteKielitaitovaatimus(
    kieliKoodiUri = kieli.map(_.koodiUri),
    kielitaidonVoiOsoittaa = kielitaidonVoiOsoittaa.map(_.toKielitaito),
    vaatimukset = vaatimukset.map(_.toKielitaitovaatimus)
  )
}

case class KielitaidonVoiOsoittaaIndexed(kielitaito: Option[KoodiUri], lisatieto: Kielistetty) {
  def toKielitaito: Kielitaito = Kielitaito(kielitaito.map(_.koodiUri), lisatieto = lisatieto)
}

case class KielitaitovaatimusIndexed(
    kielitaitovaatimus: Option[KoodiUri],
    kielitaitovaatimusKuvaukset: Seq[KielitaitovaatimusKuvausIndexed]
) {
  def toKielitaitovaatimus: Kielitaitovaatimus = Kielitaitovaatimus(
    kielitaitovaatimusKoodiUri = kielitaitovaatimus.map(_.koodiUri),
    kielitaitovaatimusKuvaukset = kielitaitovaatimusKuvaukset.map(_.toKielitaitovaatimusKuvaus)
  )
}

case class KielitaitovaatimusKuvausIndexed(
    kielitaitovaatimusKuvaus: Option[KoodiUri],
    kielitaitovaatimusTaso: Option[String]
) {
  def toKielitaitovaatimusKuvaus: KielitaitovaatimusKuvaus = KielitaitovaatimusKuvaus(
    kielitaitovaatimusKuvausKoodiUri = kielitaitovaatimusKuvaus.map(_.koodiUri),
    kielitaitovaatimusTaso = kielitaitovaatimusTaso
  )
}

case class ValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) {
  def toValintatapa: Valintatapa = Valintatapa(
    nimi = nimi,
    valintatapaKoodiUri = valintatapa.map(_.koodiUri),
    kuvaus = kuvaus,
    sisalto = sisalto,
    kaytaMuuntotaulukkoa = kaytaMuuntotaulukkoa,
    kynnysehto = kynnysehto,
    enimmaispisteet = enimmaispisteet,
    vahimmaispisteet = vahimmaispisteet
  )
}
