package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime

import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.enums.{Julkaisutila, Kieli, Koulutustyyppi}
import fi.oph.kouta.external.domain.oid.{KoulutusOid, ToteutusOid}

case class ToteutusIndexed(
    oid: Option[ToteutusOid],
    koulutusOid: KoulutusOid,
    tila: Julkaisutila,
    tarjoajat: List[Organisaatio],
    nimi: Kielistetty,
    metadata: Option[ToteutusMetadataIndexed],
    muokkaaja: Muokkaaja,
    organisaatio: Organisaatio,
    kielivalinta: Seq[Kieli],
    teemakuva: Option[String],
    modified: Option[LocalDateTime]
) {
  def toToteutus = Toteutus(
    oid = oid,
    koulutusOid = koulutusOid,
    tila = tila,
    tarjoajat = tarjoajat.map(_.oid),
    nimi = nimi,
    metadata = metadata.map(_.toToteutusMetadata),
    muokkaaja = muokkaaja.oid,
    organisaatioOid = organisaatio.oid,
    kielivalinta = kielivalinta,
    teemakuva = teemakuva,
    modified = modified
  )
}

case class ToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisalaIndexed],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
) {
  def toToteutusMetadata: ToteutusMetadata = tyyppi match {
    case Koulutustyyppi.Amm =>
      AmmatillinenToteutusMetadata(
        tyyppi = tyyppi,
        kuvaus = kuvaus,
        osaamisalat = osaamisalat.map(_.toAmmatillinenOsaamisala),
        opetus = opetus.map(_.toOpetus),
        asiasanat = asiasanat,
        ammattinimikkeet = ammattinimikkeet,
        yhteyshenkilot = yhteyshenkilot
      )
    case Koulutustyyppi.Yo =>
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
    case Koulutustyyppi.Amk =>
      AmmattikorkeakouluToteutusMetadata(
        tyyppi = tyyppi,
        kuvaus = kuvaus,
        opetus = opetus.map(_.toOpetus),
        asiasanat = asiasanat,
        ammattinimikkeet = ammattinimikkeet,
        yhteyshenkilot = yhteyshenkilot,
        alemmanKorkeakoulututkinnonOsaamisalat = alemmanKorkeakoulututkinnonOsaamisalat,
        ylemmanKorkeakoulututkinnonOsaamisalat = ylemmanKorkeakoulututkinnonOsaamisalat
      )
    case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
  }
}

case class AmmatillinenOsaamisalaIndexed(koodi: KoodiUri, linkki: Kielistetty, otsikko: Kielistetty) {
  def toAmmatillinenOsaamisala = AmmatillinenOsaamisala(koodiUri = koodi.koodiUri, linkki = linkki, otsikko = otsikko)
}

case class OpetusIndexed(
    opetuskieli: Seq[KoodiUri],
    opetuskieletKuvaus: Kielistetty,
    opetusaika: Seq[KoodiUri],
    opetusaikaKuvaus: Kielistetty,
    opetustapa: Seq[KoodiUri],
    opetustapaKuvaus: Kielistetty,
    onkoMaksullinen: Option[Boolean],
    maksullisuusKuvaus: Kielistetty,
    maksunMaara: Option[Double],
    koulutuksenTarkkaAlkamisaika: Boolean,
    koulutuksenAlkamispaivamaara: Option[LocalDateTime],
    koulutuksenPaattymispaivamaara: Option[LocalDateTime],
    koulutuksenAlkamiskausi: Option[String],
    koulutuksenAlkamisvuosi: Option[Int],
    lisatiedot: Seq[LisatietoIndexed],
    onkoStipendia: Option[Boolean],
    stipendinMaara: Option[Double],
    stipendinKuvaus: Kielistetty
) {
  def toOpetus: Opetus = Opetus(
    opetuskieliKoodiUrit = opetuskieli.map(_.koodiUri),
    opetuskieletKuvaus = opetuskieletKuvaus,
    opetusaikaKoodiUrit = opetusaika.map(_.koodiUri),
    opetusaikaKuvaus = opetusaikaKuvaus,
    opetustapaKoodiUrit = opetustapa.map(_.koodiUri),
    opetustapaKuvaus = opetustapaKuvaus,
    onkoMaksullinen = onkoMaksullinen,
    maksullisuusKuvaus = maksullisuusKuvaus,
    maksunMaara = maksunMaara,
    koulutuksenTarkkaAlkamisaika = koulutuksenTarkkaAlkamisaika,
    koulutuksenAlkamispaivamaara = koulutuksenAlkamispaivamaara,
    koulutuksenPaattymispaivamaara = koulutuksenPaattymispaivamaara,
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi,
    koulutuksenAlkamisvuosi = koulutuksenAlkamisvuosi,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    onkoStipendia = onkoStipendia,
    stipendinMaara = stipendinMaara,
    stipendinKuvaus = stipendinKuvaus
  )
}
