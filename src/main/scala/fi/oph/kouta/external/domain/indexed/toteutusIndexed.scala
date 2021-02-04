package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime

import fi.oph.kouta.domain._
import fi.oph.kouta.domain.oid.{KoulutusOid, ToteutusOid}
import fi.oph.kouta.external.domain._

case class ToteutusIndexed(
    oid: Option[ToteutusOid],
    koulutusOid: Option[KoulutusOid],
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
  def toToteutus: Toteutus = Toteutus(
    oid = oid,
    koulutusOid = koulutusOid.get,
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

case class OpetusIndexed(
    opetuskieli: Seq[KoodiUri],
    opetuskieletKuvaus: Kielistetty,
    opetusaika: Seq[KoodiUri],
    opetusaikaKuvaus: Kielistetty,
    opetustapa: Seq[KoodiUri],
    opetustapaKuvaus: Kielistetty,
    onkoMaksullinen: Option[Boolean],
    maksullisuusKuvaus: Kielistetty,
    koulutuksenAlkamiskausiUUSI: Option[KoulutuksenAlkamiskausiIndexed],
    maksunMaara: Option[Double],
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
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausiUUSI.map(_.toKoulutuksenAlkamiskausi),
    maksunMaara = maksunMaara,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    onkoStipendia = onkoStipendia,
    stipendinMaara = stipendinMaara,
    stipendinKuvaus = stipendinKuvaus
  )
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
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisalaIndexed],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  def toToteutusMetadata: AmmatillinenToteutusMetadata = AmmatillinenToteutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    osaamisalat = osaamisalat.map(_.toAmmatillinenOsaamisala),
    opetus = opetus.map(_.toOpetus),
    asiasanat = asiasanat,
    ammattinimikkeet = ammattinimikkeet,
    yhteyshenkilot = yhteyshenkilot
  )
}

case class AmmatillinenOsaamisalaIndexed(koodi: KoodiUri, linkki: Kielistetty, otsikko: Kielistetty) {
  def toAmmatillinenOsaamisala: AmmatillinenOsaamisala =
    AmmatillinenOsaamisala(koodiUri = koodi.koodiUri, linkki = linkki, otsikko = otsikko)
}

case class AmmattikorkeakouluToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
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
    tyyppi: Koulutustyyppi,
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
