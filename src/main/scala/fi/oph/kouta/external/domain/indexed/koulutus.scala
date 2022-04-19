package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain._
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain._

import java.util.UUID

case class KoulutusIndexed(
    oid: Option[KoulutusOid],
    externalId: Option[String],
    johtaaTutkintoon: Boolean,
    koulutustyyppi: Koulutustyyppi,
    koulutukset: Seq[KoodiUri],
    tila: Julkaisutila,
    tarjoajat: List[Organisaatio],
    julkinen: Boolean,
    kielivalinta: Seq[Kieli],
    nimi: Kielistetty,
    metadata: Option[KoulutusMetadataIndexed],
    sorakuvausId: Option[UUID],
    muokkaaja: Muokkaaja,
    organisaatio: Organisaatio,
    teemakuva: Option[String],
    ePerusteId: Option[Long],
    modified: Option[Modified]
) {
  def toKoulutus: Koulutus = Koulutus(
    oid = oid,
    externalId = externalId,
    johtaaTutkintoon = johtaaTutkintoon,
    koulutustyyppi = koulutustyyppi,
    koulutuksetKoodiUri = koulutukset.map(_.koodiUri),
    tila = tila,
    tarjoajat = tarjoajat.map(_.oid),
    nimi = nimi,
    metadata = metadata.map(_.toKoulutusMetadata),
    sorakuvausId = sorakuvausId,
    julkinen = julkinen,
    muokkaaja = muokkaaja.oid,
    organisaatioOid = organisaatio.oid,
    kielivalinta = kielivalinta,
    teemakuva = teemakuva,
    ePerusteId = ePerusteId,
    modified = modified
  )
}

sealed trait KoulutusMetadataIndexed {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val lisatiedot: Seq[LisatietoIndexed]

  def toKoulutusMetadata: KoulutusMetadata
}

case class AmmatillinenKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Amm,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenKoulutusMetadata =
    AmmatillinenKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto)
    )
}

case class AmmatillinenTutkinnonOsaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmTutkinnonOsa,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    tutkinnonOsat: Seq[TutkinnonOsaIndexed] = Seq.empty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenTutkinnonOsaKoulutusMetadata =
    AmmatillinenTutkinnonOsaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      tutkinnonOsat = tutkinnonOsat.map(_.toTutkinnonOsa)
    )
}

case class AmmatillinenOsaamisalaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOsaamisala,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    osaamisala: Option[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenOsaamisalaKoulutusMetadata =
    AmmatillinenOsaamisalaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      osaamisalaKoodiUri = osaamisala.map(_.koodiUri)
    )
}

sealed trait KorkeakoulutusKoulutusMetadataIndexed extends KoulutusMetadataIndexed {
  val kuvauksenNimi: Kielistetty
  val tutkintonimike: Seq[KoodiUri]
  val opintojenLaajuus: Option[KoodiUri]
  val koulutusala: Seq[KoodiUri]
}

case class YliopistoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Yo,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    koulutusala: Seq[KoodiUri] = Seq.empty,
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: YliopistoKoulutusMetadata = YliopistoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi
  )
}

case class AmmattikorkeakouluKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Amk,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    koulutusala: Seq[KoodiUri] = Seq.empty,
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmattikorkeakouluKoulutusMetadata = AmmattikorkeakouluKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi
  )
}

case class LukioKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    opintojenLaajuus: Option[KoodiUri],
    koulutusala: Seq[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: LukioKoulutusMetadata = LukioKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri)
  )
}

case class TuvaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Tuva,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    linkkiEPerusteisiin: Kielistetty = Map.empty,
    opintojenLaajuus: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TuvaKoulutusMetadata = TuvaKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    linkkiEPerusteisiin = linkkiEPerusteisiin,
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri)
  )
}

case class TelmaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Telma,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    linkkiEPerusteisiin: Kielistetty = Map.empty,
    opintojenLaajuus: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TelmaKoulutusMetadata = TelmaKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    linkkiEPerusteisiin = linkkiEPerusteisiin,
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri)
  )
}

case class VapaaSivistystyoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusKoodiUri: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: VapaaSivistystyoKoulutusMetadata =
    VapaaSivistystyoKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusKoodiUri = opintojenLaajuusKoodiUri.map(_.koodiUri)
    )
}

case class AikuistenPerusopetusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusKoodiUri: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AikuistenPerusopetusKoulutusMetadata =
    AikuistenPerusopetusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusKoodiUri = opintojenLaajuusKoodiUri.map(_.koodiUri)
    )
}
