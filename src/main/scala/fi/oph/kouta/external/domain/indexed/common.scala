package fi.oph.kouta.external.domain.indexed

import java.util.UUID

import fi.oph.kouta.external.domain.{Ajanjakso, Kielistetty, Lisatieto, Osoite, Valintakoe, Valintakoetilaisuus}
import fi.oph.kouta.external.domain.oid.{OrganisaatioOid, UserOid}

case class Muokkaaja(oid: UserOid)

case class Organisaatio(oid: OrganisaatioOid)

case class KoodiUri(koodiUri: String)

case class UuidObject(id: UUID)

case class LisatietoIndexed(otsikko: KoodiUri, teksti: Kielistetty) {
  def toLisatieto: Lisatieto = Lisatieto(otsikkoKoodiUri = otsikko.koodiUri, teksti)
}

case class ValintakoeIndexed(id: Option[UUID], tyyppi: Option[KoodiUri], tilaisuudet: List[ValintakoetilaisuusIndexed]) {
  def toValintakoe: Valintakoe = Valintakoe(
    id = id,
    tyyppiKoodiUri = tyyppi.map(_.koodiUri),
    tilaisuudet = tilaisuudet.map(_.toValintakoetilaisuus)
  )
}

case class ValintakoetilaisuusIndexed(
    osoite: Option[OsoiteIndexed],
    aika: Option[Ajanjakso],
    lisatietoja: Kielistetty
) {
  def toValintakoetilaisuus: Valintakoetilaisuus = Valintakoetilaisuus(
    osoite = osoite.map(_.toOsoite),
    aika = aika,
    lisatietoja = lisatietoja
  )
}

case class OsoiteIndexed(osoite: Kielistetty, postinumero: Option[KoodiUri]) {
  def toOsoite: Osoite = Osoite(osoite, postinumero.map(_.koodiUri))
}
