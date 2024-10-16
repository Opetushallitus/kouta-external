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
    esikatselu: Option[Boolean],
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
    esikatselu = esikatselu,
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
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    tutkintonimike: Seq[KoodiUri] = Seq.empty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None,
    koulutusala: Seq[KoodiUri] = Seq.empty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenKoulutusMetadata =
    AmmatillinenKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero,
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri)
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

case class AmmatillinenMuuKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmMuu,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    koulutusala: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenMuuKoulutusMetadata =
    AmmatillinenMuuKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

sealed trait KorkeakoulutusKoulutusMetadataIndexed extends KoulutusMetadataIndexed {
  val tutkintonimike: Seq[KoodiUri]
  val opintojenLaajuusyksikko: Option[KoodiUri]
  val opintojenLaajuusNumero: Option[Double]
  val koulutusala: Seq[KoodiUri]
}

case class YliopistoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Yo,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    koulutusala: Seq[KoodiUri] = Seq.empty,
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: YliopistoKoulutusMetadata = YliopistoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class AmmattikorkeakouluKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Amk,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    koulutusala: Seq[KoodiUri] = Seq.empty,
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmattikorkeakouluKoulutusMetadata = AmmattikorkeakouluKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class AmmOpeErityisopeJaOpoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmOpeErityisopeJaOpoKoulutusMetadata = AmmOpeErityisopeJaOpoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class OpePedagOpinnotKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = OpePedagOpinnot,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: OpePedagOpinnotKoulutusMetadata = OpePedagOpinnotKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class LukioKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None,
    koulutusala: Seq[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: LukioKoulutusMetadata = LukioKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero,
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri)
  )
}

case class TuvaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Tuva,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    linkkiEPerusteisiin: Kielistetty = Map.empty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TuvaKoulutusMetadata = TuvaKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    linkkiEPerusteisiin = linkkiEPerusteisiin,
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class TelmaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Telma,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    linkkiEPerusteisiin: Kielistetty = Map.empty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TelmaKoulutusMetadata = TelmaKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    linkkiEPerusteisiin = linkkiEPerusteisiin,
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class VapaaSivistystyoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: VapaaSivistystyoKoulutusMetadata =
    VapaaSivistystyoKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

case class VapaaSivistystyoOsaamismerkkiKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed] = Seq(),
    linkkiEPerusteisiin: Kielistetty = Map(),
    opintojenLaajuusyksikko: Option[KoodiUri],
    opintojenLaajuusNumero: Option[Double],
    osaamismerkkiKoodiUri: Option[String],
    koulutusala: Seq[KoodiUri] = Seq()
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: VapaaSivistystyoOsaamismerkkiKoulutusMetadata =
    VapaaSivistystyoOsaamismerkkiKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero,
      osaamismerkkiKoodiUri = osaamismerkkiKoodiUri,
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri)
    )
}

case class AikuistenPerusopetusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AikuistenPerusopetus,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AikuistenPerusopetusKoulutusMetadata =
    AikuistenPerusopetusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

case class KkOpintojaksoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri] = Seq.empty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    isAvoinKorkeakoulutus: Option[Boolean],
    tunniste: Option[String] = None,
    opinnonTyyppi: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KkOpintojaksoKoulutusMetadata =
    KkOpintojaksoKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax,
      isAvoinKorkeakoulutus = isAvoinKorkeakoulutus,
      tunniste = tunniste,
      opinnonTyyppiKoodiUri = opinnonTyyppi.map(_.koodiUri)
    )
}

case class ErikoislaakariKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Erikoislaakari,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    koulutusala: Seq[KoodiUri] = Seq.empty,
    tutkintonimike: Seq[KoodiUri] = Seq.empty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: ErikoislaakariKoulutusMetadata =
    ErikoislaakariKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri)
    )
}

case class KkOpintokokonaisuusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    opintojenLaajuusyksikko: Option[KoodiUri],
    isAvoinKorkeakoulutus: Option[Boolean],
    tunniste: Option[String] = None,
    opinnonTyyppi: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KkOpintokokonaisuusKoulutusMetadata =
    KkOpintokokonaisuusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      isAvoinKorkeakoulutus = isAvoinKorkeakoulutus,
      tunniste = tunniste,
      opinnonTyyppiKoodiUri = opinnonTyyppi.map(_.koodiUri)
    )
}

case class ErikoistumiskoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Erikoistumiskoulutus,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    erikoistumiskoulutus: Option[KoodiUri],
    koulutusala: Seq[KoodiUri] = Seq.empty,
    opintojenLaajuusyksikko: Option[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    korkeakoulutustyypit: Seq[Korkeakoulutustyyppi] = Seq()
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: ErikoistumiskoulutusMetadata =
    ErikoistumiskoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      erikoistumiskoulutusKoodiUri = erikoistumiskoulutus.map(_.koodiUri),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax,
      korkeakoulutustyypit = korkeakoulutustyypit
    )
}

case class TaiteenPerusopetusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TaiteenPerusopetusKoulutusMetadata =
    TaiteenPerusopetusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin
    )
}

case class MuuKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Muu,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri] = Seq.empty,
    opintojenLaajuusyksikko: Option[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: MuuKoulutusMetadata =
    MuuKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax
    )
}
