package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain._
import fi.oph.kouta.domain.oid.{KoulutusOid, ToteutusOid}
import fi.oph.kouta.external.domain._

import java.util.UUID

case class ToteutusIndexed(
    oid: Option[ToteutusOid],
    externalId: Option[String],
    koulutusOid: Option[KoulutusOid],
    tila: Julkaisutila,
    tarjoajat: List[Organisaatio],
    nimi: Kielistetty,
    metadata: Option[ToteutusMetadataIndexed],
    sorakuvausId: Option[UUID],
    muokkaaja: Muokkaaja,
    organisaatio: Organisaatio,
    kielivalinta: Seq[Kieli],
    teemakuva: Option[String],
    modified: Option[Modified]
) {
  def toToteutus: Toteutus = Toteutus(
    oid = oid,
    externalId = externalId,
    koulutusOid = koulutusOid.get,
    tila = tila,
    tarjoajat = tarjoajat.map(_.oid),
    nimi = nimi,
    metadata = metadata.map(_.toToteutusMetadata),
    sorakuvausId = sorakuvausId,
    muokkaaja = muokkaaja.oid,
    organisaatioOid = organisaatio.oid,
    kielivalinta = kielivalinta,
    teemakuva = teemakuva,
    modified = modified
  )
}

case class OpetusIndexed(
    opetuskieli: Seq[KoodiUri],
    opetuskieletKuvaus: Kielistetty,
    opetusaika: Seq[KoodiUri],
    opetusaikaKuvaus: Kielistetty,
    opetustapa: Seq[KoodiUri],
    opetustapaKuvaus: Kielistetty,
    maksullisuustyyppi: Option[Maksullisuustyyppi],
    maksullisuusKuvaus: Kielistetty,
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiIndexed],
    maksunMaara: Option[Double],
    lisatiedot: Seq[LisatietoIndexed],
    onkoApuraha: Boolean,
    apuraha: Option[ApurahaIndexed],
    suunniteltuKestoVuodet: Option[Int],
    suunniteltuKestoKuukaudet: Option[Int],
    suunniteltuKestoKuvaus: Kielistetty
) {
  def toOpetus: Opetus = Opetus(
    opetuskieliKoodiUrit = opetuskieli.map(_.koodiUri),
    opetuskieletKuvaus = opetuskieletKuvaus,
    opetusaikaKoodiUrit = opetusaika.map(_.koodiUri),
    opetusaikaKuvaus = opetusaikaKuvaus,
    opetustapaKoodiUrit = opetustapa.map(_.koodiUri),
    opetustapaKuvaus = opetustapaKuvaus,
    maksullisuustyyppi = maksullisuustyyppi,
    maksullisuusKuvaus = maksullisuusKuvaus,
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.map(_.toKoulutuksenAlkamiskausi),
    maksunMaara = maksunMaara,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    onkoApuraha = onkoApuraha,
    apuraha = apuraha.map(_.toApuraha),
    suunniteltuKestoVuodet = suunniteltuKestoVuodet,
    suunniteltuKestoKuukaudet = suunniteltuKestoKuukaudet,
    suunniteltuKestoKuvaus = suunniteltuKestoKuvaus
  )
}

case class ApurahaIndexed(min: Option[Int], max: Option[Int], yksikko: Option[Apurahayksikko], kuvaus: Kielistetty) {
  def toApuraha: Apuraha = Apuraha(min = min, max = max, yksikko = yksikko, kuvaus = kuvaus)
}

trait ToteutusMetadataIndexed {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val opetus: Option[OpetusIndexed]
  val asiasanat: List[Keyword]
  val ammattinimikkeet: List[Keyword]
  val yhteyshenkilot: Seq[Yhteyshenkilo]

  def toToteutusMetadata: ToteutusMetadata
}

trait KorkeakoulutusToteutusMetadata extends ToteutusMetadataIndexed {
  val alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
  val ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
}

case class AmmatillinenToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Amm,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisalaIndexed],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    ammatillinenPerustutkintoErityisopetuksena: Boolean
) extends ToteutusMetadataIndexed {
  def toToteutusMetadata: AmmatillinenToteutusMetadata = AmmatillinenToteutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    osaamisalat = osaamisalat.map(_.toAmmatillinenOsaamisala),
    opetus = opetus.map(_.toOpetus),
    asiasanat = asiasanat,
    ammattinimikkeet = ammattinimikkeet,
    yhteyshenkilot = yhteyshenkilot,
    ammatillinenPerustutkintoErityisopetuksena = ammatillinenPerustutkintoErityisopetuksena
  )
}

case class AmmatillinenOsaamisalaIndexed(koodi: KoodiUri, linkki: Kielistetty, otsikko: Kielistetty) {
  def toAmmatillinenOsaamisala: AmmatillinenOsaamisala =
    AmmatillinenOsaamisala(koodiUri = koodi.koodiUri, linkki = linkki, otsikko = otsikko)
}

trait TutkintoonJohtamatonToteutusMetadataIndexed extends ToteutusMetadataIndexed {
  val hakutermi: Option[Hakutermi]
  val hakulomaketyyppi: Option[Hakulomaketyyppi]
  val hakulomakeLinkki: Kielistetty
  val lisatietoaHakeutumisesta: Kielistetty
  val lisatietoaValintaperusteista: Kielistetty
  val hakuaika: Option[Ajanjakso]
  val aloituspaikat: Option[Int]
}

case class AmmatillinenTutkinnonOsaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmTutkinnonOsa,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  def toToteutusMetadata: AmmatillinenTutkinnonOsaToteutusMetadata =
    AmmatillinenTutkinnonOsaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat
    )
}

case class AmmatillinenOsaamisalaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOsaamisala,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  def toToteutusMetadata: AmmatillinenOsaamisalaToteutusMetadata =
    AmmatillinenOsaamisalaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat
    )
}

case class AmmatillinenMuuToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmMuu,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  def toToteutusMetadata: AmmatillinenMuuToteutusMetadata =
    AmmatillinenMuuToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat
    )
}

case class AmmattikorkeakouluToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Amk,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
) extends KorkeakoulutusToteutusMetadata {
  def toToteutusMetadata: AmmattikorkeakouluToteutusMetadata = AmmattikorkeakouluToteutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    opetus = opetus.map(_.toOpetus),
    asiasanat = asiasanat,
    ammattinimikkeet = ammattinimikkeet,
    yhteyshenkilot = yhteyshenkilot,
    alemmanKorkeakoulututkinnonOsaamisalat = alemmanKorkeakoulututkinnonOsaamisalat,
    ylemmanKorkeakoulututkinnonOsaamisalat = ylemmanKorkeakoulututkinnonOsaamisalat
  )
}

case class YliopistoToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Yo,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
) extends ToteutusMetadataIndexed {
  def toToteutusMetadata: YliopistoToteutusMetadata =
    YliopistoToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      alemmanKorkeakoulututkinnonOsaamisalat = alemmanKorkeakoulututkinnonOsaamisalat,
      ylemmanKorkeakoulututkinnonOsaamisalat = ylemmanKorkeakoulututkinnonOsaamisalat
    )
}

case class KielivalikoimaIndexed(
    A1Kielet: Seq[KoodiUri] = Seq(),
    A2Kielet: Seq[KoodiUri] = Seq(),
    B1Kielet: Seq[KoodiUri] = Seq(),
    B2Kielet: Seq[KoodiUri] = Seq(),
    B3Kielet: Seq[KoodiUri] = Seq(),
    aidinkielet: Seq[KoodiUri] = Seq(),
    muutKielet: Seq[KoodiUri] = Seq()
) {
  def toKielivalikoima: Kielivalikoima =
    Kielivalikoima(
      A1Kielet = A1Kielet.map(_.koodiUri),
      A2Kielet = A2Kielet.map(_.koodiUri),
      B1Kielet = B1Kielet.map(_.koodiUri),
      B2Kielet = B2Kielet.map(_.koodiUri),
      B3Kielet = B3Kielet.map(_.koodiUri),
      aidinkielet = aidinkielet.map(_.koodiUri),
      muutKielet = muutKielet.map(_.koodiUri)
    )
}

case class LukiolinjaTietoIndexed(koodi: KoodiUri, kuvaus: Kielistetty) {
  def toLukioLinjaTieto: LukiolinjaTieto = LukiolinjaTieto(koodi.koodiUri, kuvaus)
}

case class LukiodiplomiTietoIndexed(koodi: KoodiUri, linkki: Kielistetty, linkinAltTeksti: Kielistetty) {
  def toLukioDiplomiTieto: LukiodiplomiTieto = LukiodiplomiTieto(koodi.koodiUri, linkki, linkinAltTeksti)
}

case class LukioToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    kielivalikoima: Option[KielivalikoimaIndexed],
    yleislinja: Boolean,
    painotukset: Seq[LukiolinjaTietoIndexed],
    erityisetKoulutustehtavat: Seq[LukiolinjaTietoIndexed],
    diplomit: Seq[LukiodiplomiTietoIndexed]
) extends ToteutusMetadataIndexed {
  def toToteutusMetadata: LukioToteutusMetadata =
    LukioToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      kielivalikoima = kielivalikoima.map(_.toKielivalikoima),
      yleislinja = yleislinja,
      painotukset = painotukset.map(_.toLukioLinjaTieto),
      erityisetKoulutustehtavat = erityisetKoulutustehtavat.map(_.toLukioLinjaTieto),
      diplomit = diplomit.map(_.toLukioDiplomiTieto)
    )
}

case class TuvaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    aloituspaikat: Option[Int],
    jarjestetaanErityisopetuksena: Boolean
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: TuvaToteutusMetadata = {
    TuvaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      aloituspaikat = aloituspaikat,
      jarjestetaanErityisopetuksena = jarjestetaanErityisopetuksena
    )
  }
}

case class TelmaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    aloituspaikat: Option[Int]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: TelmaToteutusMetadata = {
    TelmaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      aloituspaikat = aloituspaikat
    )
  }
}

case class VapaaSivistystyoOpistovuosiToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: VapaaSivistystyoOpistovuosiToteutusMetadata = {
    VapaaSivistystyoOpistovuosiToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class VapaaSivistystyoMuuToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: VapaaSivistystyoMuuToteutusMetadata = {
    VapaaSivistystyoMuuToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat
    )
  }
}

case class AikuistenPerusopetusToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AikuistenPerusopetus,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: AikuistenPerusopetusToteutusMetadata = {
    AikuistenPerusopetusToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat
    )
  }
}
