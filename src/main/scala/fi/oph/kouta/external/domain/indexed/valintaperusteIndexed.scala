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

case class AmmatillinenValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Amm,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AmmatillinenValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class LukioValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    LukioValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class YliopistoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Yo,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    YliopistoValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class AmmattikorkeakouluValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Amk,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AmmattikorkeakouluValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class AmmatillinenTutkinnonOsaValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmTutkinnonOsa,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AmmatillinenTutkinnonOsaValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class AmmatillinenOsaamisalaValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOsaamisala,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AmmatillinenOsaamisalaValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class TuvaValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Tuva,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    TuvaValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class TelmaValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Telma,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    TelmaValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class AmmatillinenMuuValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmMuu,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AmmatillinenMuuValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class MuuValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Muu,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty,
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    MuuValintaperusteMetadata(
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
    kuvaus: Kielistetty,
    sisalto: Seq[Sisalto],
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

case class VapaaSivistystyoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    VapaaSivistystyoValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class AikuistenPerusopetusValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata =
    AikuistenPerusopetusValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}
