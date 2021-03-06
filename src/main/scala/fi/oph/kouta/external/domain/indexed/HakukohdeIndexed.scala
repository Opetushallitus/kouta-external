package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.domain._
import fi.oph.kouta.external.domain.{Ajanjakso, Hakukohde, HakukohdeMetadata, Kielistetty}

import java.time.LocalDateTime
import java.util.UUID

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
    metadata: Option[HakukohdeMetadataIndexed],
    organisaatio: Organisaatio,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified],
    toteutus: Option[Tarjoajat],
    jarjestyspaikka: Option[Organisaatio]
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
    metadata = metadata.map(_.toHakukohdeMetadata),
    organisaatioOid = organisaatio.oid,
    kielivalinta = kielivalinta,
    modified = modified,
    jarjestyspaikkaOid = jarjestyspaikka.map(_.oid)
  )

  def tarjoajat: Seq[OrganisaatioOid] =
    toteutus.map(_.tarjoajat.map(_.oid)).getOrElse(Seq())
}

case class Tarjoajat(tarjoajat: Seq[Organisaatio])

class HakukohdeMetadataIndexed(valintakokeidenYleiskuvaus: Kielistetty,
                               koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiIndexed],
                               kaytetaanHaunAlkamiskautta: Option[Boolean]
                              ) {
  def toHakukohdeMetadata: HakukohdeMetadata = HakukohdeMetadata(
    valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus,
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.map(_.toKoulutuksenAlkamiskausi),
    kaytetaanHaunAlkamiskautta = kaytetaanHaunAlkamiskautta
  )
}

