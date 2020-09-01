package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.domain.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa}
import fi.oph.kouta.external.domain.{Ajanjakso, Hakukohde, Kielistetty}

case class HakukohdeIndexed(
    oid: Option[HakukohdeOid],
    toteutusOid: ToteutusOid,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    alkamiskausi: Option[KoodiUri],
    alkamisvuosi: Option[String],
    kaytetaanHaunAlkamiskautta: Option[Boolean],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    kaytetaanHaunHakulomaketta: Option[Boolean],
    aloituspaikat: Option[Int],
    ensikertalaisenAloituspaikat: Option[Int],
    pohjakoulutusvaatimus: Seq[KoodiUri],
    pohjakoulutusvaatimusTarkenne: Kielistetty,
    muuPohjakoulutusvaatimus: Kielistetty,
    toinenAsteOnkoKaksoistutkinto: Option[Boolean],
    kaytetaanHaunAikataulua: Option[Boolean],
    valintaperuste: Option[UuidObject],
    liitteetOnkoSamaToimitusaika: Option[Boolean],
    liitteetOnkoSamaToimitusosoite: Option[Boolean],
    liitteidenToimitusaika: Option[LocalDateTime],
    liitteidenToimitustapa: Option[LiitteenToimitustapa],
    liitteidenToimitusosoite: Option[LiitteenToimitusosoiteIndexed],
    liitteet: List[LiiteIndexed],
    valintakokeet: List[ValintakoeIndexed],
    hakuajat: List[Ajanjakso],
    muokkaaja: Muokkaaja,
    organisaatio: Organisaatio,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    toteutus: Option[Tarjoajat]
) {
  def toHakukohde: Hakukohde = Hakukohde(
    oid = oid,
    toteutusOid = toteutusOid,
    hakuOid = hakuOid,
    tila = tila,
    nimi = nimi,
    alkamiskausiKoodiUri = alkamiskausi.map(_.koodiUri),
    alkamisvuosi = alkamisvuosi,
    kaytetaanHaunAlkamiskautta = kaytetaanHaunAlkamiskautta,
    hakulomaketyyppi = hakulomaketyyppi,
    hakulomakeAtaruId = hakulomakeAtaruId,
    hakulomakeKuvaus = hakulomakeKuvaus,
    hakulomakeLinkki = hakulomakeLinkki,
    kaytetaanHaunHakulomaketta = kaytetaanHaunHakulomaketta,
    aloituspaikat = aloituspaikat,
    ensikertalaisenAloituspaikat = ensikertalaisenAloituspaikat,
    pohjakoulutusvaatimusKoodiUrit = pohjakoulutusvaatimus.map(_.koodiUri),
    pohjakoulutusvaatimusTarkenne = pohjakoulutusvaatimusTarkenne,
    muuPohjakoulutusvaatimus = muuPohjakoulutusvaatimus,
    toinenAsteOnkoKaksoistutkinto = toinenAsteOnkoKaksoistutkinto,
    kaytetaanHaunAikataulua = kaytetaanHaunAikataulua,
    valintaperusteId = valintaperuste.map(_.id),
    liitteetOnkoSamaToimitusaika = liitteetOnkoSamaToimitusaika,
    liitteetOnkoSamaToimitusosoite = liitteetOnkoSamaToimitusosoite,
    liitteidenToimitusaika = liitteidenToimitusaika,
    liitteidenToimitustapa = liitteidenToimitustapa,
    liitteidenToimitusosoite = liitteidenToimitusosoite.map(_.toLiitteenToimitusosoite),
    liitteet = liitteet.map(_.toLiite),
    valintakokeet = valintakokeet.map(_.toValintakoe),
    hakuajat = hakuajat,
    muokkaaja = muokkaaja.oid,
    organisaatioOid = organisaatio.oid,
    kielivalinta = kielivalinta,
    modified = modified
  )

  def tarjoajat: Seq[OrganisaatioOid] =
    toteutus.map(_.tarjoajat.map(_.oid)).getOrElse(Seq())
}

case class Tarjoajat(tarjoajat: Seq[Organisaatio])
