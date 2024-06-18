package fi.oph.kouta.external.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import fi.oph.kouta.domain.oid._
import fi.oph.kouta.domain._
import fi.oph.kouta.external.domain._

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Try

case class OsoiteES @JsonCreator() (
    @JsonProperty("osoite") osoite: Map[String, String] = Map(),
    @JsonProperty("postinumeroKoodiUri") postinumeroKoodiUri: Map[String, PostinumeroKoodiES] = Map()
)

case class LiitteenToimitusosoiteES @JsonCreator() (
    @JsonProperty("osoite") osoite: Option[OsoiteES],
    @JsonProperty("sahkoposti") sahkoposti: Option[String],
    @JsonProperty("verkkosivu") verkkosivu: Option[String]
)

case class LiiteTyyppiES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)

case class LiiteES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tyyppi") tyyppi: Option[LiiteTyyppiES],
    @JsonProperty("nimi") nimi: Map[String, String] = Map(),
    @JsonProperty("kuvaus") kuvaus: Map[String, String] = Map(),
    @JsonProperty("toimitusaika") toimitusaika: Option[String],
    @JsonProperty("toimitustapa") toimitustapa: Option[String],
    @JsonProperty("toimitusosoite") toimitusosoite: Option[LiitteenToimitusosoiteES]
)

case class LiitteetToimitusosoiteOsoite @JsonCreator() (
    @JsonProperty("osoite") osoite: Map[String, String],
    @JsonProperty("postinumero") postinumero: String
)

case class ValintakoeTilaisuus @JsonCreator() (
    @JsonProperty("aika") aika: Option[AikaJakso],
    @JsonProperty("jarjestamispaikka") jarjestamispaikka: Map[String, String] = Map(),
    @JsonProperty("lisatietoja") lisatietoja: Map[String, String] = Map(),
    @JsonProperty("osoite") osoite: Option[OsoiteES]
)

case class ValintaKoeMetadataES @JsonCreator() (
    @JsonProperty("liittyyEnnakkovalmistautumista") liittyyEnnakkovalmistautumista: Option[Boolean],
    @JsonProperty("ohjeetEnnakkovalmistautumiseen") ohjeetEnnakkovalmistautumiseen: Map[String, String] = Map(),
    @JsonProperty("erityisjarjestelytMahdollisia") erityisjarjestelytMahdollisia: Option[Boolean],
    @JsonProperty("ohjeetErityisjarjestelyihin") ohjeetErityisjarjestelyihin: Map[String, String] = Map(),
    @JsonProperty("tietoja") tietoja: Map[String, String] = Map(),
    @JsonProperty("vahimmaispisteet") vahimmaispisteet: Option[Double]
)

case class ValintakoeTyyppi @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)

case class AloituspaikatES @JsonCreator() (
    @JsonProperty("kuvaus") kuvaus: Map[String, String],
    @JsonProperty("lukumaara") lukumaara: Int,
    @JsonProperty("ensikertalaisille") ensikertalaisille: Int
)

case class KoulutuksenAlkamiskausiMapES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)

case class HakukohdeMetadataES @JsonCreator() (
    @JsonProperty("aloituspaikat") aloituspaikat: AloituspaikatES,
    @JsonProperty("kaytetaanHaunAlkamiskautta") kaytetaanHaunAlkamiskautta: Boolean,
    @JsonProperty("kynnysehto") kynnysehto: Map[String, String],
    @JsonProperty("uudenOpiskelijanUrl") uudenOpiskelijanUrl: Map[String, String],
    @JsonProperty("valintakokeidenYleiskuvaus") valintakokeidenYleiskuvaus: Map[String, String],
    @JsonProperty("valintaperusteenValintakokeidenLisatilaisuudet") valintaperusteenValintakokeidenLisatilaisuudet: Seq[
      ValintakoeLisatilaisuusIndexedES
    ],
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiHakukohdeES],
    @JsonProperty("hakukohteenLinja") hakukohteenLinja: Option[HakukohteenLinjaES]
)

case class HakukohteenLinjaES @JsonCreator() (
    @JsonProperty("linja") linja: Option[KoodiES],
    @JsonProperty("alinHyvaksyttyKeskiarvo") alinHyvaksyttyKeskiarvo: Option[String],
    @JsonProperty("lisatietoa") lisatietoa: Map[String, String] = Map()
)

case class KoodiES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)

case class PostinumeroKoodiES @JsonCreator()(
                                   @JsonProperty("koodiUri") koodiUri: String,
                                   @JsonProperty("nimi") nimi: String
                                 )

case class ValintakoeLisatilaisuusIndexedES @JsonCreator() (
    @JsonProperty("id") id: Option[String],
    @JsonProperty("tilaisuudet") tilaisuudet: Seq[ValintakoetilaisuusES] = Seq()
)

case class ValintakoetilaisuusES @JsonCreator() (
    @JsonProperty("aika") aika: Option[AikaJakso],
    @JsonProperty("jarjestamispaikka") jarjestamispaikka: Map[String, String],
    @JsonProperty("lisatietoja") lisatietoja: Map[String, String],
    @JsonProperty("osoite") osoite: Option[OsoiteES]
)

case class TarjoajaES @JsonCreator() (
    @JsonProperty("oid") oid: String
)

case class ToteutusES @JsonCreator() (
    @JsonProperty("tarjoajat") tarjoajat: List[TarjoajaES]
)

case class PaateltyAlkamiskausiES @JsonCreator() (
    @JsonProperty("alkamiskausityyppi") alkamiskausityyppi: String,
    @JsonProperty("kausiUri") kausiUri: String,
    @JsonProperty("source") source: String,
    @JsonProperty("vuosi") vuosi: String
)

case class ValintaperusteES @JsonCreator() (
  @JsonProperty("id") id: String
)

case class HakukohdeJavaClient @JsonCreator() (
    @JsonProperty("oid") oid: Option[String],
    @JsonProperty("externalId") externalId: Option[String],
    @JsonProperty("toteutusOid") toteutusOid: String,
    @JsonProperty("hakuOid") hakuOid: String,
    @JsonProperty("tila") tila: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map(),
    @JsonProperty("hakulomaketyyppi") hakulomaketyyppi: Option[String],
    @JsonProperty("hakulomakeAtaruId") hakulomakeAtaruId: Option[String],
    @JsonProperty("hakulomakeKuvaus") hakulomakeKuvaus: Map[String, String] = Map(),
    @JsonProperty("hakulomakeLinkki") hakulomakeLinkki: Map[String, String] = Map(),
    @JsonProperty("kaytetaanHaunHakulomaketta") kaytetaanHaunHakulomaketta: Option[Boolean],
    @JsonProperty("pohjakoulutusvaatimus") pohjakoulutusvaatimus: List[KoodiES] = List(),
    @JsonProperty("pohjakoulutusvaatimusTarkenne") pohjakoulutusvaatimusTarkenne: Map[String, String] = Map(),
    @JsonProperty("muuPohjakoulutusvaatimus") muuPohjakoulutusvaatimus: Map[String, String] = Map(),
    @JsonProperty("toinenAsteOnkoKaksoistutkinto") toinenAsteOnkoKaksoistutkinto: Option[Boolean],
    @JsonProperty("kaytetaanHaunAikataulua") kaytetaanHaunAikataulua: Option[Boolean],
    @JsonProperty("valintaperuste") valintaperuste: Option[ValintaperusteES],
    @JsonProperty("liitteetOnkoSamaToimitusaika") liitteetOnkoSamaToimitusaika: Option[Boolean],
    @JsonProperty("liitteetOnkoSamaToimitusosoite") liitteetOnkoSamaToimitusosoite: Option[Boolean],
    @JsonProperty("liitteidenToimitusaika") liitteidenToimitusaika: Option[String],
    @JsonProperty("liitteidenToimitustapa") liitteidenToimitustapa: Option[String],
    @JsonProperty("liitteidenToimitusosoite") liitteidenToimitusosoiteES: Option[LiitteenToimitusosoiteES],
    @JsonProperty("liitteet") liitteet: List[LiiteES] = List(),
    @JsonProperty("valintakokeet") valintakokeet: List[ValintakoeES] = List(),
    @JsonProperty("hakuajat") hakuajat: List[AikaJakso] = List(),
    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
    @JsonProperty("metadata") metadata: Option[HakukohdeMetadataES],
    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
    @JsonProperty("kielivalinta") kielivalinta: Seq[String] = Seq(),
    @JsonProperty("modified") modified: String,
    @JsonProperty("toteutus") toteutus: ToteutusES,
    @JsonProperty("johtaaTutkintoon") johtaaTutkintoon: Boolean,
    @JsonProperty("opetuskieliKoodiUrit") opetuskieliKoodiUrit: Seq[String],
    @JsonProperty("koulutusasteKoodiUrit") koulutusasteKoodiUrit: Seq[String],
    @JsonProperty("hakutapaKoodiUri") hakutapaKoodiUri: Option[String],
    @JsonProperty("paateltyAlkamiskausi") paateltyAlkamiskausi: Option[PaateltyAlkamiskausiES]
) extends WithKielistettyOsoite {

  def toResult(): HakukohdeIndexed = {
    HakukohdeIndexed(
      oid = oid.map(HakukohdeOid),
      externalId = externalId,
      toteutusOid = ToteutusOid(toteutusOid),
      hakuOid = HakuOid(hakuOid),
      tila = Julkaisutila.withName(tila),
      nimi = toKielistettyMap(nimi),
      jarjestyspaikka = Some(Organisaatio(OrganisaatioOid(organisaatio.oid))),
      hakulomaketyyppi = hakulomaketyyppi.map(Hakulomaketyyppi.withName),
      hakulomakeAtaruId = hakulomakeAtaruId.flatMap(id => Try(UUID.fromString(id)).toOption),
      hakulomakeKuvaus = toKielistettyMap(hakulomakeKuvaus),
      hakulomakeLinkki = toKielistettyMap(hakulomakeLinkki),
      kaytetaanHaunHakulomaketta = kaytetaanHaunHakulomaketta,
      pohjakoulutusvaatimus = pohjakoulutusvaatimus.map(p => KoodiUri(p.koodiUri)),
      pohjakoulutusvaatimusTarkenne = toKielistettyMap(pohjakoulutusvaatimusTarkenne),
      muuPohjakoulutusvaatimus = toKielistettyMap(muuPohjakoulutusvaatimus),
      toinenAsteOnkoKaksoistutkinto = toinenAsteOnkoKaksoistutkinto,
      kaytetaanHaunAikataulua = kaytetaanHaunAikataulua,
      valintaperuste = valintaperuste.map(vp => UuidObject(UUID.fromString(vp.id))),
      liitteetOnkoSamaToimitusaika = liitteetOnkoSamaToimitusaika,
      liitteetOnkoSamaToimitusosoite = liitteetOnkoSamaToimitusosoite,
      liitteidenToimitusaika = liitteidenToimitusaika.map(LocalDateTime.parse),
      liitteidenToimitustapa = liitteidenToimitustapa.map(LiitteenToimitustapa.withName),
      liitteidenToimitusosoite = getOsoite(liitteidenToimitusosoiteES),
      liitteet = getLiitteet(liitteet),
      valintakokeet = getValintakokeet(valintakokeet),
      hakuajat = hakuajat.map(hakuaika => {
        Ajanjakso(parseLocalDateTime(hakuaika.alkaa), hakuaika.paattyy.map(parseLocalDateTime))
      }),
      muokkaaja = Muokkaaja(UserOid(muokkaaja.oid)),
      metadata = getHakukohdeMetadataIndexed(metadata),
      organisaatio = Organisaatio(OrganisaatioOid(organisaatio.oid)),
      kielivalinta = kielivalinta.map(kieli => Kieli.withName(kieli)),
      modified = Some(Modified(LocalDateTime.parse(modified))),
      toteutus = Some(Tarjoajat(toteutus.tarjoajat.map(tarjoaja => Organisaatio(OrganisaatioOid(tarjoaja.oid))))),
      johtaaTutkintoon = Option.apply(johtaaTutkintoon),
      opetuskieliKoodiUrit = opetuskieliKoodiUrit,
      koulutusasteKoodiUrit = koulutusasteKoodiUrit,
      hakutapaKoodiUri = hakutapaKoodiUri,
      paateltyAlkamiskausi = paateltyAlkamiskausi.map(pa =>
        PaateltyAlkamiskausi(
          alkamiskausityyppi = Option.apply(Alkamiskausityyppi.withName(pa.alkamiskausityyppi)),
          kausiUri = Option.apply(pa.kausiUri),
          vuosi = Option.apply(pa.vuosi)
        )
      )
    )
  }

  def toKielistettyMap(map: Map[String, String]): Kielistetty = {
    Map(
      En -> map.get("en"),
      Fi -> map.get("fi"),
      Sv -> map.get("sv")
    ).collect { case (k, Some(v)) => (k, v) }
  }
  def parseLocalDateTime(dateString: String): LocalDateTime = {
    LocalDateTime.parse(dateString)
  }
  def getHakukohdeMetadataIndexed(metadataESOption: Option[HakukohdeMetadataES]): Option[HakukohdeMetadataIndexed] = {
    metadataESOption.map(metadataES => HakukohdeMetadataIndexed(
      valintakokeidenYleiskuvaus = toKielistettyMap(metadataES.valintakokeidenYleiskuvaus),
      kynnysehto = toKielistettyMap(metadataES.kynnysehto),
      valintaperusteenValintakokeidenLisatilaisuudet =
        metadataES.valintaperusteenValintakokeidenLisatilaisuudet.map(lisaTilaisuus => {
          ValintakokeenLisatilaisuudetIndexed(
            id = lisaTilaisuus.id.map(UUID.fromString),
            tilaisuudet = lisaTilaisuus.tilaisuudet.map(tilaisuus =>
              ValintakoetilaisuusIndexed(
                osoite = getOsoiteIndexed(tilaisuus.osoite),
                aika = tilaisuus.aika.map(aika =>
                  Ajanjakso(
                    parseLocalDateTime(aika.alkaa),
                    aika.paattyy.map(parseLocalDateTime)
                  )
                ),
                lisatietoja = toKielistettyMap(tilaisuus.lisatietoja),
                jarjestamispaikka = toKielistettyMap(tilaisuus.jarjestamispaikka)
              )
            )
          )
        }),
      koulutuksenAlkamiskausi = metadataES.koulutuksenAlkamiskausi.map(koulutuksenAlkamiskausi =>
        KoulutuksenAlkamiskausiIndexed(
          alkamiskausityyppi =
            koulutuksenAlkamiskausi.alkamiskausityyppi.map(Alkamiskausityyppi.withName),
          henkilokohtaisenSuunnitelmanLisatiedot =
            toKielistettyMap(koulutuksenAlkamiskausi.henkilokohtaisenSuunnitelmanLisatiedot),
          koulutuksenAlkamispaivamaara =
            koulutuksenAlkamiskausi.koulutuksenAlkamispaivamaara.map(parseLocalDateTime),
          koulutuksenPaattymispaivamaara =
            koulutuksenAlkamiskausi.koulutuksenPaattymispaivamaara.map(parseLocalDateTime),
          koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.koulutuksenAlkamiskausi.map(ka => KoodiUri(ka.koodiUri)),
          koulutuksenAlkamisvuosi = koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi
        )
      ),
      kaytetaanHaunAlkamiskautta = Option.apply(metadataES.kaytetaanHaunAlkamiskautta),
      aloituspaikat = Option.apply(
        Aloituspaikat(
          lukumaara = Option.apply(metadataES.aloituspaikat.lukumaara),
          ensikertalaisille = Option.apply(metadataES.aloituspaikat.ensikertalaisille),
          kuvaus = toKielistettyMap(metadataES.aloituspaikat.kuvaus)
        )
      ),
      hakukohteenLinja = metadataES.hakukohteenLinja.map(hakukohteenLinja =>
        HakukohteenLinja(
          linja = hakukohteenLinja.linja.map(koodi => Koodi(Some(koodi.koodiUri))),
          alinHyvaksyttyKeskiarvo = hakukohteenLinja.alinHyvaksyttyKeskiarvo.flatMap(ka => Try(ka.toDouble).toOption),
          lisatietoa = toKielistettyMap(hakukohteenLinja.lisatietoa)
        )
      )
    ))
  }
  def getValintakokeet(valintakoeList: List[ValintakoeES]): List[ValintakoeIndexed] = {
    valintakoeList.map(koe => {
      ValintakoeIndexed(
        id = Try(UUID.fromString(koe.id)).toOption,
        tyyppi = koe.tyyppi.map(t => KoodiUri(t.koodiUri)),
        nimi = toKielistettyMap(koe.nimi),
        metadata = koe.metadata.map(metadata =>
          ValintaKoeMetadataIndexed(
            tietoja = toKielistettyMap(metadata.tietoja),
            vahimmaispisteet = metadata.vahimmaispisteet,
            liittyyEnnakkovalmistautumista = metadata.liittyyEnnakkovalmistautumista,
            ohjeetEnnakkovalmistautumiseen = toKielistettyMap(metadata.ohjeetEnnakkovalmistautumiseen),
            erityisjarjestelytMahdollisia = metadata.erityisjarjestelytMahdollisia,
            ohjeetErityisjarjestelyihin = toKielistettyMap(metadata.ohjeetErityisjarjestelyihin)
          )
        ),
        tilaisuudet = koe.tilaisuudet.map(tilaisuus => {
          ValintakoetilaisuusIndexed(
            osoite = toKielistettyOsoite(tilaisuus.osoite),
            aika = tilaisuus.aika.map(aika =>
              Ajanjakso(
                alkaa = parseLocalDateTime(aika.alkaa),
                paattyy = aika.paattyy.map(parseLocalDateTime)
              )
            ),
            lisatietoja = toKielistettyMap(tilaisuus.lisatietoja),
            jarjestamispaikka = toKielistettyMap(tilaisuus.jarjestamispaikka)
          )
        })
      )
    })

  }
  def getLiitteet(liitteet: List[LiiteES]): List[LiiteIndexed] = {
    liitteet.map(liite => {
        LiiteIndexed(
          id = Try(UUID.fromString(liite.id)).toOption,
          tyyppi = liite.tyyppi.map(tyyppi => KoodiUri(tyyppi.koodiUri)),
          nimi = toKielistettyMap(liite.nimi),
          kuvaus = toKielistettyMap(liite.kuvaus),
          toimitusaika = liite.toimitusaika.map(parseLocalDateTime),
          toimitustapa = liite.toimitustapa.map(LiitteenToimitustapa.withName),
          toimitusosoite = liite.toimitusosoite.map(toimitusosoite =>
            LiitteenToimitusosoiteIndexed(
              toKielistettyOsoite(toimitusosoite.osoite).get,
              toimitusosoite.sahkoposti,
              toimitusosoite.verkkosivu
            )
          )
        )
      })
  }
  def getOsoite(liitteenToimitusosoiteOption: Option[LiitteenToimitusosoiteES]): Option[LiitteenToimitusosoiteIndexed] = {
    liitteenToimitusosoiteOption.map(liitteenToimitusosoite =>
      LiitteenToimitusosoiteIndexed(
        toKielistettyOsoite(liitteenToimitusosoite.osoite).get,
        liitteenToimitusosoite.sahkoposti,
        liitteenToimitusosoite.verkkosivu
      )
    )
  }

  def getOsoiteIndexed(osoiteEs: Option[OsoiteES]): Option[OsoiteIndexed] = {
    toKielistettyOsoite(osoiteEs)
  }
}
case class HakukohdeIndexed(
    oid: Option[fi.oph.kouta.domain.oid.HakukohdeOid],
    externalId: Option[String],
    toteutusOid: ToteutusOid,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    jarjestyspaikka: Option[Organisaatio],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    kaytetaanHaunHakulomaketta: Option[Boolean],
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
    johtaaTutkintoon: Option[Boolean],
    opetuskieliKoodiUrit: Seq[String],
    koulutusasteKoodiUrit: Seq[String],
    hakutapaKoodiUri: Option[String],
    paateltyAlkamiskausi: Option[PaateltyAlkamiskausi]
) {
  def toHakukohde(hakukohderyhmat: Option[Seq[HakukohderyhmaOid]]): Hakukohde = Hakukohde(
    oid = oid,
    externalId = externalId,
    toteutusOid = toteutusOid,
    hakuOid = hakuOid,
    tila = tila,
    nimi = nimi,
    tarjoaja = jarjestyspaikka.map(_.oid),
    hakulomaketyyppi = hakulomaketyyppi,
    hakulomakeAtaruId = hakulomakeAtaruId,
    hakulomakeKuvaus = hakulomakeKuvaus,
    hakulomakeLinkki = hakulomakeLinkki,
    hakukohderyhmat = hakukohderyhmat,
    kaytetaanHaunHakulomaketta = kaytetaanHaunHakulomaketta,
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
    johtaaTutkintoon = johtaaTutkintoon,
    hakutapaKoodiUri = hakutapaKoodiUri,
    opetuskieliKoodiUrit = opetuskieliKoodiUrit,
    koulutusasteKoodiUrit = koulutusasteKoodiUrit,
    paateltyAlkamiskausi = paateltyAlkamiskausi
  )

  def tarjoajat: Seq[OrganisaatioOid] =
    toteutus.map(_.tarjoajat.map(_.oid)).getOrElse(Seq())
}

case class Tarjoajat(tarjoajat: Seq[Organisaatio])

case class HakukohdeMetadataIndexed(
    valintakokeidenYleiskuvaus: Kielistetty,
    kynnysehto: Kielistetty,
    valintaperusteenValintakokeidenLisatilaisuudet: Seq[ValintakokeenLisatilaisuudetIndexed] = Seq(),
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiIndexed],
    kaytetaanHaunAlkamiskautta: Option[Boolean],
    aloituspaikat: Option[Aloituspaikat],
    hakukohteenLinja: Option[HakukohteenLinja]
) {
  def toHakukohdeMetadata: HakukohdeMetadata = HakukohdeMetadata(
    valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus,
    kynnysehto = kynnysehto,
    valintaperusteenValintakokeidenLisatilaisuudet =
      valintaperusteenValintakokeidenLisatilaisuudet.map(_.toValintakokeenLisatilaisuudet),
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.map(_.toKoulutuksenAlkamiskausi),
    kaytetaanHaunAlkamiskautta = kaytetaanHaunAlkamiskautta,
    aloituspaikat = aloituspaikat,
    hakukohteenLinja = hakukohteenLinja
  )
}

case class ValintakokeenLisatilaisuudetIndexed(id: Option[UUID], tilaisuudet: Seq[ValintakoetilaisuusIndexed] = Seq()) {
  def toValintakokeenLisatilaisuudet: ValintakokeenLisatilaisuudet = ValintakokeenLisatilaisuudet(
    id = id,
    tilaisuudet = tilaisuudet.map(_.toValintakoetilaisuus)
  )
}

case class HakukohdeIndexedTest(
    oid: Option[HakukohdeOid],
    externalId: Option[String],
    toteutusOid: ToteutusOid,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    jarjestyspaikka: Option[Organisaatio],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    kaytetaanHaunHakulomaketta: Option[Boolean],
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
    johtaaTutkintoon: Option[Boolean],
    opetuskieliKoodiUrit: Seq[String],
    koulutusasteKoodiUrit: Seq[String],
    hakutapaKoodiUri: Option[String],
    paateltyAlkamiskausi: Option[PaateltyAlkamiskausi]
)
