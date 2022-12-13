package fi.oph.kouta.external.domain.indexed

import java.util.UUID
import fi.oph.kouta.domain._
import fi.oph.kouta.external.domain._

case class ValintaperusteIndexed(
    id: Option[UUID],
    externalId: Option[String],
    tila: Julkaisutila,
    koulutustyyppi: Koulutustyyppi,
    hakutapa: Option[KoodiUri],
    kohdejoukko: Option[KoodiUri],
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
    externalId = externalId,
    koulutustyyppi = koulutustyyppi,
    tila = tila,
    hakutapaKoodiUri = hakutapa.map(_.koodiUri),
    kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
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
  def kuvaus: Kielistetty
  def hakukelpoisuus: Kielistetty
  def lisatiedot: Kielistetty
  def valintakokeidenYleiskuvaus: Kielistetty
  def sisalto: Seq[Sisalto]

  def toValintaperusteMetadata: ValintaperusteMetadata
}

case class GenericValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata =
    GenericValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class ValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    sisalto: Seq[Sisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) {
  def toValintatapa: Valintatapa = Valintatapa(
    nimi = nimi,
    valintatapaKoodiUri = valintatapa.map(_.koodiUri),
    sisalto = sisalto,
    kaytaMuuntotaulukkoa = kaytaMuuntotaulukkoa,
    kynnysehto = kynnysehto,
    enimmaispisteet = enimmaispisteet,
    vahimmaispisteet = vahimmaispisteet
  )
}
