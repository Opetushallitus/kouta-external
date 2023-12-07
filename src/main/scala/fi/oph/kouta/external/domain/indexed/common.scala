package fi.oph.kouta.external.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import fi.oph.kouta.domain.Alkamiskausityyppi
import fi.oph.kouta.domain.oid.{OrganisaatioOid, UserOid}
import fi.oph.kouta.external.domain._
import fi.oph.kouta.domain.{oid, _}

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
                             nimi: Kielistetty,
                             metadata: Option[ValintaKoeMetadataIndexed],
                             tilaisuudet: List[ValintakoetilaisuusIndexed]) {
  def toValintakoe: Valintakoe = Valintakoe(
    id = id,
    tyyppiKoodiUri = tyyppi.map(_.koodiUri),
    nimi = nimi,
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


case class KoulutuksenAlkamiskausiHakukohdeES @JsonCreator()(
  @JsonProperty("alkamiskausityyppi") alkamiskausityyppi: String,
  @JsonProperty("henkilokohtaisenSuunnitelmanLisatiedot") henkilokohtaisenSuunnitelmanLisatiedot: Map[String, String],
  @JsonProperty("koulutuksenAlkamispaivamaara") koulutuksenAlkamispaivamaara: String,
  @JsonProperty("koulutuksenPaattymispaivamaara") koulutuksenPaattymispaivamaara: String,
  @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: KoulutuksenAlkamiskausiMapES,
  @JsonProperty("koulutuksenAlkamisvuosi") koulutuksenAlkamisvuosi: String)

case class AikaJakso @JsonCreator() (
    @JsonProperty("alkaa") alkaa: String,
    @JsonProperty("formatoituAlkaa") formatoituAlkaa: Map[String, String],
    @JsonProperty("formatoituPaattyy") formatoituPaattyy: Map[String, String],
    @JsonProperty("paattyy") paattyy: String
)

case class MuokkaajaES @JsonCreator() (@JsonProperty("nimi") nimi: String, @JsonProperty("oid") oid: String)

case class OrganisaatioES @JsonCreator() (@JsonProperty("oid") oid: String)

case class ValintakoeES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tyyppi") tyyppi: ValintakoeTyyppi,
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("metadata") metadata: ValintaKoeMetadataES,
    @JsonProperty("tilaisuudet") tilaisuudet: List[ValintakoeTilaisuus]
)
