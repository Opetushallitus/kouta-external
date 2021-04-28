package fi.oph.kouta.external.domain.indexed

import java.util.UUID

import fi.oph.kouta.domain._
import fi.oph.kouta.external.domain._

case class ValintaperusteIndexed(
    id: Option[UUID],
    tila: Julkaisutila,
    koulutustyyppi: Koulutustyyppi,
    hakutapa: Option[KoodiUri],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    nimi: Kielistetty,
    julkinen: Boolean,
    valintakokeet: List[ValintakoeIndexed],
    metadata: Option[ValintaperusteMetadataIndexed],
    organisaatio: Organisaatio,
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified]
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
  def valintakokeidenYleiskuvaus: Kielistetty
  def kuvaus: Kielistetty

  def toValintaperusteMetadata: ValintaperusteMetadata
}

case class AmmatillinenValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Amm,
    valintatavat: Seq[ValintatapaIndexed],
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
    kuvaus: Kielistetty
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AmmatillinenValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus,
      kuvaus = kuvaus
    )
}

sealed trait KorkeakoulutusValintaperusteMetadataIndexed extends ValintaperusteMetadataIndexed {
  def osaamistausta: Seq[KoodiUri]
}

case class YliopistoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
    osaamistausta: Seq[KoodiUri],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: YliopistoValintaperusteMetadata = YliopistoValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toValintatapa),
    valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus,
    osaamistaustaKoodiUrit = osaamistausta.map(_.koodiUri),
    kuvaus = kuvaus
  )
}

case class AmmattikorkeakouluValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
    osaamistausta: Seq[KoodiUri],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: AmmattikorkeakouluValintaperusteMetadata =
    AmmattikorkeakouluValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus,
      osaamistaustaKoodiUrit = osaamistausta.map(_.koodiUri),
      kuvaus = kuvaus
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
