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

case class ValintakoeIndexed(id: Option[UUID],
                             tyyppi: Option[KoodiUri],
                             metadata: Option[ValintaKoeMetadataIndexed],
                             tilaisuudet: List[ValintakoetilaisuusIndexed]) {
  def toValintakoe: Valintakoe = Valintakoe(
    id = id,
    tyyppiKoodiUri = tyyppi.map(_.koodiUri),
    metadata = metadata.map(_.toValintakoeMetadata),
    tilaisuudet = tilaisuudet.map(_.toValintakoetilaisuus)
  )
}

case class ValintaKoeMetadataIndexed(tietoja: Kielistetty = Map(),
                                     vahimmaispisteet: Option[Double] = None,
                                     liittyyEnnakkovalmistautumista: Option[Boolean] = None,
                                     ohjeetEnnakkovalmistautumiseen: Kielistetty = Map(),
                                     erityisjarjestelytMahdollisia: Option[Boolean] = None,
                                     ohjeetErityisjarjestelyihin: Kielistetty = Map()) {
  def toValintakoeMetadata: ValintaKoeMetadata = ValintaKoeMetadata(
    tietoja = tietoja,
    vahimmaispisteet = vahimmaispisteet,
    liittyyEnnakkovalmistautumista = liittyyEnnakkovalmistautumista,
    ohjeetEnnakkovalmistautumiseen = ohjeetEnnakkovalmistautumiseen,
    erityisjarjestelytMahdollisia = erityisjarjestelytMahdollisia,
    ohjeetErityisjarjestelyihin = ohjeetErityisjarjestelyihin
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

case class TutkinnonOsaIndexed(ePerusteId: Option[Long] = None,
                               koulutusKoodiUri: Option[KoodiUri] = None,
                               tutkinnonosaId: Option[Long] = None,
                               tutkinnonosaViite: Option[Long] = None) {
  def toTutkinnonOsa: TutkinnonOsa = TutkinnonOsa(
    ePerusteId = ePerusteId,
    koulutusKoodiUri = koulutusKoodiUri.map(_.koodiUri),
    tutkinnonosaId = tutkinnonosaId,
    tutkinnonosaViite = tutkinnonosaViite
  )
}
