package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.domain.Alkamiskausityyppi
import fi.oph.kouta.domain.oid.{OrganisaatioOid, UserOid}
import fi.oph.kouta.external.domain._

import java.time.LocalDateTime
import java.util.UUID

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
    lisatietoja: Kielistetty,
    jarjestamispaikka: Kielistetty = Map()
) {
  def toValintakoetilaisuus: Valintakoetilaisuus = Valintakoetilaisuus(
    osoite = osoite.map(_.toOsoite),
    aika = aika,
    lisatietoja = lisatietoja,
    jarjestamispaikka = jarjestamispaikka
  )
}

case class OsoiteIndexed(osoite: Kielistetty, postinumero: Option[KoodiUri]) {
  def toOsoite: Osoite = Osoite(osoite, postinumero.map(_.koodiUri))
}

case class KoulutuksenAlkamiskausiIndexed(alkamiskausityyppi: Option[Alkamiskausityyppi],
                                          henkilokohtaisenSuunnitelmanLisatiedot: Kielistetty,
                                          koulutuksenAlkamispaivamaara: Option[LocalDateTime],
                                          koulutuksenPaattymispaivamaara: Option[LocalDateTime],
                                          koulutuksenAlkamiskausi: Option[KoodiUri],
                                          koulutuksenAlkamisvuosi: Option[String]) {
  def toKoulutuksenAlkamiskausi: KoulutuksenAlkamiskausi = KoulutuksenAlkamiskausi(
    alkamiskausityyppi = alkamiskausityyppi,
    henkilokohtaisenSuunnitelmanLisatiedot = henkilokohtaisenSuunnitelmanLisatiedot,
    koulutuksenAlkamispaivamaara = koulutuksenAlkamispaivamaara,
    koulutuksenPaattymispaivamaara = koulutuksenPaattymispaivamaara,
    koulutuksenAlkamiskausiKoodiUri = koulutuksenAlkamiskausi.map(_.koodiUri),
    koulutuksenAlkamisvuosi = koulutuksenAlkamisvuosi
  )
}
