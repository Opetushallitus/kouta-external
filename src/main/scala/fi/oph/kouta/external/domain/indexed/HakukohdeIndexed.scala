package fi.oph.kouta.external.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonIgnoreProperties, JsonProperty, JsonUnwrapped}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, HakukohderyhmaOid, Oid, OrganisaatioOid, ToteutusOid, UserOid}
import fi.oph.kouta.domain.{oid, _}
import fi.oph.kouta.external.domain.{Ajanjakso, Aloituspaikat, Hakukohde, HakukohdeMetadata, HakukohteenLinja, Kielistetty, Koodi, KoodiNimi, PaateltyAlkamiskausi, ValintakokeenLisatilaisuudet}
import fi.oph.kouta.external.elasticsearch.ResultEntity

import java.time.LocalDateTime
import java.util
import java.util.UUID
import scala.runtime.Nothing$

// Haluttu luokkarakenne
case class Result(oid: Option[Oid], oid2: Option[Oid], oid3: Option[Oid])
case class LiitteenToimitusosoiteIndexedES(
    osoite: OsoiteIndexed,
    sahkoposti: Option[String],
    verkkosivu: Option[String]
)
case class liiteES(id: String)
case class OsoiteES @JsonCreator() (
    @JsonProperty("osoite") osoite: Map[String, String],
    @JsonProperty("postinumeroKoodiUri") postinumeroKoodiUri: String
)
case class LiitteenToimitusosoiteES @JsonCreator() (
    @JsonProperty("osoite") osoite: OsoiteES,
    @JsonProperty("sahkoposti") sahkoposti: Option[String],
    @JsonProperty("verkkosivu") verkkosivu: Option[String]
)
case class LiiteTyyppiES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)
case class LiiteES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tyyppi") tyyppi: LiiteTyyppiES,
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("kuvaus") kuvaus: Map[String, String],
    @JsonProperty("toimitusaika") toimitusaika: String,
    @JsonProperty("toimitustapa") toimitustapa: String,
    @JsonProperty("toimitusosoite") toimitusosoite: LiitteenToimitusosoiteES
)
case class LiitteetToimitusosoite @JsonCreator() (
    @JsonProperty("osoite") osoite: LiitteetToimitusosoiteOsoite,
    @JsonProperty("sahkoposti") sahkoposti: Option[String],
    @JsonProperty("verkkosivu") verkkosivu: Option[String]
)
case class LiitteetToimitusosoiteOsoite @JsonCreator() (
    @JsonProperty("osoite") osoite: Map[String, String],
    @JsonProperty("postinumero") postinumero: String
)

case class ValintakoeES @JsonCreator() (
    @JsonProperty("id") id: String,                                     //id: Option[UUID],
    @JsonProperty("tyyppi") tyyppi: ValintakoeTyyppi,                   // tyyppi: Option[KoodiUri],
    @JsonProperty("nimi") nimi: Map[String, String],                    // nimi: Kielistetty,
    @JsonProperty("metadata") metadata: ValintaKoeMetadataES,             // Option[ValintaKoeMetadataIndexed],
    @JsonProperty("tilaisuudet") tilaisuudet: List[ValintakoeTilaisuus] // List[ValintakoetilaisuusIndexed]
)

case class AikaJakso @JsonCreator() (
    @JsonProperty("alkaa") alkaa: String,
    @JsonProperty("formatoituAlkaa") formatoituAlkaa: Map[String, String],
    @JsonProperty("formatoituPaattyy") formatoituPaattyy: Map[String, String],
    @JsonProperty("paattyy") paattyy: String
)
case class ValintakoeTilaisuus @JsonCreator() (
    @JsonProperty("aika") aika: AikaJakso,
    @JsonProperty("jarjestamispaikka") jarjestamispaikka: Map[String, String],
    @JsonProperty("lisatietoja") lisatietoja: Map[String, String],
    @JsonProperty("osoite") osoite: OsoiteES
)
case class ValintaKoeMetadataES @JsonCreator() (
    @JsonProperty("liittyyEnnakkovalmistautumista") liittyyEnnakkovalmistautumista: Boolean,
    @JsonProperty("ohjeetEnnakkovalmistautumiseen") ohjeetEnnakkovalmistautumiseen: Map[String, String],
    @JsonProperty("erityisjarjestelytMahdollisia") erityisjarjestelytMahdollisia: Boolean,
    @JsonProperty("ohjeetErityisjarjestelyihin") ohjeetErityisjarjestelyihin: Map[String, String],
    @JsonProperty("tietoja") tietoja: Map[String, String],
    @JsonProperty("vahimmaispisteet") vahimmaispisteet: Double
)
case class ValintakoeTyyppi @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] // nimi: Kielistetty,
)
case class MuokkaajaES @JsonCreator() (@JsonProperty("nimi") nimi: String, @JsonProperty("oid") oid: String)

case class AloituspaikatES @JsonCreator() (
    @JsonProperty("kuvaus") kuvaus: Map[String, String],
    @JsonProperty("lukumaara") lukumaara: Int,
    @JsonProperty("ensikertalaisille") ensikertalaisille: Int
)
case class KoulutuksenAlkamiskausiES @JsonCreator() (

    @JsonProperty("alkamiskausityyppi") alkamiskausityyppi: String,
    @JsonProperty("henkilokohtaisenSuunnitelmanLisatiedot") henkilokohtaisenSuunnitelmanLisatiedot: Map[String, String],
    @JsonProperty("koulutuksenAlkamispaivamaara") koulutuksenAlkamispaivamaara: String,
    @JsonProperty("koulutuksenPaattymispaivamaara") koulutuksenPaattymispaivamaara: String,
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: KoulutuksenAlkamiskausiMapES,
    @JsonProperty("koulutuksenAlkamisvuosi") koulutuksenAlkamisvuosi: String
)
case class KoulutuksenAlkamiskausiMapES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
                                                       )
case class MetadataES @JsonCreator() (

    @JsonProperty("aloituspaikat") aloituspaikat: AloituspaikatES,
    @JsonProperty("kaytetaanHaunAlkamiskautta") kaytetaanHaunAlkamiskautta: Boolean,
    @JsonProperty("kynnysehto") kynnysehto: Map[String, String],
    @JsonProperty("uudenOpiskelijanUrl") uudenOpiskelijanUrl: Map[String, String],
    @JsonProperty("valintakokeidenYleiskuvaus") valintakokeidenYleiskuvaus: Map[String, String],
    @JsonProperty("valintaperusteenValintakokeidenLisatilaisuudet") valintaperusteenValintakokeidenLisatilaisuudet: Seq[
      ValintakoeLisatilaisuusIndexedES
    ],
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: KoulutuksenAlkamiskausiES,

    @JsonProperty("hakukohteenLinja") hakukohteenLinja: HakukohteenLinjaES
    //aa: HakukohteenLinja
)

case class HakukohteenLinjaES @JsonCreator() (
    @JsonProperty("linja") linja: KoodiES,
    @JsonProperty("alinHyvaksyttyKeskiarvo") alinHyvaksyttyKeskiarvo: String,
    @JsonProperty("lisatietoa") lisatietoa: Map[String, String]
)

case class KoodiES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)
case class ValintakoeLisatilaisuusIndexedES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tilaisuudet") tilaisuudet: Seq[ValintakoetilaisuusES]
)
case class ValintakoetilaisuusES @JsonCreator() (
    @JsonProperty("aika") aika: AikaJakso,
    @JsonProperty("jarjestamispaikka") jarjestamispaikka: Map[String, String],
    @JsonProperty("lisatietoja") lisatietoja: Map[String, String],
    @JsonProperty("osoite") osoite: OsoiteES
)
case class OrganisaatioES @JsonCreator() (@JsonProperty("oid") oid: String)
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
case class HakukohdeJavaClient @JsonCreator()  (
    @JsonProperty("oid") oid: String,
    @JsonProperty("externalId") externalId: String,
    @JsonProperty("toteutusOid") toteutusOid: String,
    @JsonProperty("hakuOid") hakuOid: String,
    @JsonProperty("tila") tila: String,
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("hakulomaketyyppi") hakulomaketyyppi: String,
    @JsonProperty("hakulomakeAtaruId") hakulomakeAtaruId: String,
    @JsonProperty("hakulomakeKuvaus") hakulomakeKuvaus: Map[String, String],
    @JsonProperty("hakulomakeLinkki") hakulomakeLinkki: Map[String, String],
    @JsonProperty("kaytetaanHaunHakulomaketta") kaytetaanHaunHakulomaketta: Boolean,
    @JsonProperty("pohjakoulutusvaatimus") pohjakoulutusvaatimus: List[Map[String, Object]],
    @JsonProperty("pohjakoulutusvaatimusTarkenne") pohjakoulutusvaatimusTarkenne: Map[String, String],
    @JsonProperty("muuPohjakoulutusvaatimus") muuPohjakoulutusvaatimus: Map[String, String],
    @JsonProperty("toinenAsteOnkoKaksoistutkinto") toinenAsteOnkoKaksoistutkinto: Boolean,
    @JsonProperty("kaytetaanHaunAikataulua") kaytetaanHaunAikataulua: Boolean,
    @JsonProperty("valintaperuste") valintaperuste: Map[String, Object],
    @JsonProperty("liitteetOnkoSamaToimitusaika") liitteetOnkoSamaToimitusaika: Boolean,
    @JsonProperty("liitteetOnkoSamaToimitusosoite") liitteetOnkoSamaToimitusosoite: Boolean,
    @JsonProperty("liitteidenToimitusaika") liitteidenToimitusaika: String,
    @JsonProperty("liitteidenToimitustapa") liitteidenToimitustapa: String,
    @JsonProperty("liitteidenToimitusosoite") liitteidenToimitusosoiteES: LiitteenToimitusosoiteES,
    @JsonProperty("liitteet") liitteet: List[LiiteES],
    @JsonProperty("valintakokeet") valintakokeet: List[ValintakoeES],
    @JsonProperty("hakuajat") hakuajat: List[AikaJakso],
    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
    @JsonProperty("metadata") metadata: MetadataES,
    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
    @JsonProperty("kielivalinta") kielivalinta: Seq[String],
    @JsonProperty("modified") modified: String,
    @JsonProperty("toteutus") toteutus: ToteutusES,
    @JsonProperty("johtaaTutkintoon") johtaaTutkintoon: Boolean,
    @JsonProperty("opetuskieliKoodiUrit") opetuskieliKoodiUrit: Seq[String],
    @JsonProperty("koulutusasteKoodiUrit") koulutusasteKoodiUrit: Seq[String],
    @JsonProperty("hakutapaKoodiUri") hakutapaKoodiUri: String,
    @JsonProperty("paateltyAlkamiskausi") paateltyAlkamiskausi: PaateltyAlkamiskausiES
) {

  def toResult(): HakukohdeIndexed = {

    HakukohdeIndexed(
      oid = Option.apply(oid).map(oid => HakukohdeOid(oid)),
      externalId = Option.apply(externalId),
      toteutusOid = ToteutusOid(toteutusOid),
      hakuOid = HakuOid(hakuOid),
      tila = if(tila != null) Julkaisutila.withName(tila) else null,
      nimi = toKielistettyMap(nimi),
      jarjestyspaikka = if(organisaatio != null) Option.apply(Organisaatio(OrganisaatioOid(organisaatio.oid))) else None,
      hakulomaketyyppi = Option.apply(hakulomaketyyppi).map(hakulomaketyyppi => Hakulomaketyyppi.withName(hakulomaketyyppi)),
      hakulomakeAtaruId = Option.apply(hakulomakeAtaruId).map(hakulomakeAtaruId => UUID.fromString(hakulomakeAtaruId)),
      hakulomakeKuvaus = toKielistettyMap(hakulomakeKuvaus),
      hakulomakeLinkki = toKielistettyMap(hakulomakeLinkki),
      kaytetaanHaunHakulomaketta = Option.apply(kaytetaanHaunHakulomaketta),
      pohjakoulutusvaatimus = if(pohjakoulutusvaatimus != null) pohjakoulutusvaatimus
        .map(p => {
          KoodiUri(p.get("koodiUri").toString)
        })
        .toSeq else Seq.empty,
      pohjakoulutusvaatimusTarkenne = toKielistettyMap(pohjakoulutusvaatimusTarkenne),
      muuPohjakoulutusvaatimus = toKielistettyMap(muuPohjakoulutusvaatimus),
      toinenAsteOnkoKaksoistutkinto = Option.apply(toinenAsteOnkoKaksoistutkinto),
      kaytetaanHaunAikataulua = Option.apply(kaytetaanHaunAikataulua),

      valintaperuste = if (valintaperuste != null) Option.apply(UuidObject(UUID.fromString(valintaperuste.get("id").get.toString)))
      else None,
      liitteetOnkoSamaToimitusaika = Option.apply(liitteetOnkoSamaToimitusaika),
      liitteetOnkoSamaToimitusosoite = Option.apply(liitteetOnkoSamaToimitusosoite),
      liitteidenToimitusaika = if (liitteidenToimitusaika != null) {
        Option.apply(LocalDateTime.parse(liitteidenToimitusaika))
      } else {
        None
      },
      liitteidenToimitustapa = if(liitteidenToimitustapa != null) Option.apply(LiitteenToimitustapa.withName(liitteidenToimitustapa)) else None,
      liitteidenToimitusosoite = getOsoite(liitteidenToimitusosoiteES),
      liitteet = getLiitteet(liitteet),
      valintakokeet = getValintakokeet(valintakokeet),
      hakuajat = if(hakuajat != null)hakuajat.map(hakuaika => {
        Ajanjakso(parseLocalDateTime(hakuaika.alkaa), Option.apply(parseLocalDateTime(hakuaika.paattyy)))
      }) else List.empty,
      muokkaaja = if(muokkaaja != null) Muokkaaja(UserOid(muokkaaja.oid)) else null,
      metadata = getHakukohdeMetadataIndexed(metadata),
      organisaatio = if(organisaatio != null) Organisaatio(oid = OrganisaatioOid(organisaatio.oid)) else null,
      if(kielivalinta != null) kielivalinta.map(kieli => Kieli.withName(kieli)).toSeq else Seq.empty,
      modified = if(modified != null) Option.apply(Modified(LocalDateTime.parse(modified))) else None,
      toteutus = if(toteutus != null) Option.apply(Tarjoajat(toteutus.tarjoajat.map(tarjoaja => Organisaatio(OrganisaatioOid(tarjoaja.oid))))) else None,
      johtaaTutkintoon = Option.apply(johtaaTutkintoon),
      opetuskieliKoodiUrit = opetuskieliKoodiUrit,
      koulutusasteKoodiUrit = koulutusasteKoodiUrit,
      hakutapaKoodiUri = Option.apply(hakutapaKoodiUri),
      paateltyAlkamiskausi = Option.apply(
        if(paateltyAlkamiskausi != null){
        PaateltyAlkamiskausi(
          alkamiskausityyppi = if(paateltyAlkamiskausi != null) Option.apply(Alkamiskausityyppi.withName(paateltyAlkamiskausi.alkamiskausityyppi)) else None,
          kausiUri = Option.apply(paateltyAlkamiskausi.kausiUri),
          vuosi = Option.apply(paateltyAlkamiskausi.vuosi)
        )
        } else {
          PaateltyAlkamiskausi(null, null, null)
        }
      )
    )
  }

  def toKielistettyMap(map: Map[String, String]): Kielistetty = {
    if(map != null)
    Map(
      En -> map.getOrElse("en", None).toString,
      Fi -> map.getOrElse("fi", None).toString,
      Sv -> map.getOrElse("sv", None).toString
    )
    else Map.empty
  }
  def parseLocalDateTime(dateString : String) : LocalDateTime = {
    if(dateString != null) LocalDateTime.parse(dateString) else null
  }
  def getHakukohdeMetadataIndexed(metadataES: MetadataES): Option[HakukohdeMetadataIndexed] = {
    if(metadataES != null)
    Option.apply(HakukohdeMetadataIndexed(
      valintakokeidenYleiskuvaus = toKielistettyMap(metadataES.valintakokeidenYleiskuvaus),
      kynnysehto = toKielistettyMap(metadataES.kynnysehto),
      valintaperusteenValintakokeidenLisatilaisuudet =
        metadataES.valintaperusteenValintakokeidenLisatilaisuudet.map(lisaTilaisuus => {
          ValintakokeenLisatilaisuudetIndexed(
            id = Option.apply(UUID.fromString(lisaTilaisuus.id)),
            tilaisuudet = lisaTilaisuus.tilaisuudet.map(tilaisuus =>
              ValintakoetilaisuusIndexed(
                osoite = Option.apply(getOsoiteIndexed(tilaisuus.osoite)),
                aika = Option.apply(
                  Ajanjakso(
                    parseLocalDateTime(tilaisuus.aika.alkaa),
                    Option.apply(parseLocalDateTime(tilaisuus.aika.paattyy))
                  )
                ),
                lisatietoja = toKielistettyMap(tilaisuus.lisatietoja),
                jarjestamispaikka = toKielistettyMap(tilaisuus.jarjestamispaikka)
              )
            )
          )
        }),
      koulutuksenAlkamiskausi = if (metadataES.koulutuksenAlkamiskausi != null) {
        Option.apply(
          KoulutuksenAlkamiskausiIndexed(
            alkamiskausityyppi =
              Option.apply(Alkamiskausityyppi.withName(metadataES.koulutuksenAlkamiskausi.alkamiskausityyppi)),
            henkilokohtaisenSuunnitelmanLisatiedot =
              toKielistettyMap(metadataES.koulutuksenAlkamiskausi.henkilokohtaisenSuunnitelmanLisatiedot),
            koulutuksenAlkamispaivamaara =
              Option.apply(parseLocalDateTime(metadataES.koulutuksenAlkamiskausi.koulutuksenAlkamispaivamaara)),
            koulutuksenPaattymispaivamaara =
              Option.apply( parseLocalDateTime(metadataES.koulutuksenAlkamiskausi.koulutuksenPaattymispaivamaara)),
            koulutuksenAlkamiskausi = Option.apply(
              if(metadataES.koulutuksenAlkamiskausi.koulutuksenAlkamiskausi != null) KoodiUri(metadataES.koulutuksenAlkamiskausi.koulutuksenAlkamiskausi.koodiUri) else null),
            koulutuksenAlkamisvuosi = Option.apply(metadataES.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi)
          )
        )
      } else {
        Option.apply(
          KoulutuksenAlkamiskausiIndexed(
            alkamiskausityyppi = null,
            henkilokohtaisenSuunnitelmanLisatiedot = null,
            koulutuksenAlkamispaivamaara = null,
            koulutuksenPaattymispaivamaara = null,
            koulutuksenAlkamiskausi = null,
            koulutuksenAlkamisvuosi = null
          )
        )
      },
      kaytetaanHaunAlkamiskautta = Option.apply(metadataES.kaytetaanHaunAlkamiskautta),
      aloituspaikat = Option.apply(
        Aloituspaikat(
          lukumaara = Option.apply(metadataES.aloituspaikat.lukumaara),
          ensikertalaisille = Option.apply(metadataES.aloituspaikat.ensikertalaisille),
          kuvaus = toKielistettyMap(metadataES.aloituspaikat.kuvaus)
        )
      ),
      hakukohteenLinja = if(metadata.hakukohteenLinja == null) null else {Option.apply(
        HakukohteenLinja(
          linja = None,
          alinHyvaksyttyKeskiarvo = None,
          lisatietoa = toKielistettyMap(metadata.hakukohteenLinja.lisatietoa)
        )
      )}
    ))
    else None
    //Option.apply(result)
  }
  def getValintakokeet(valintakoeList: List[ValintakoeES]): List[ValintakoeIndexed] = {
    if(valintakoeList != null)
    valintakoeList.map(koe => {
      ValintakoeIndexed(
        id = Option.apply(UUID.fromString(koe.id)),
        tyyppi = Option.apply(KoodiUri(koe.tyyppi.koodiUri)),
        nimi = toKielistettyMap(koe.nimi),
        metadata = Option.apply(
          ValintaKoeMetadataIndexed(
            tietoja = toKielistettyMap(koe.metadata.tietoja),
            vahimmaispisteet = Option.apply(koe.metadata.vahimmaispisteet),
            liittyyEnnakkovalmistautumista = Option.apply(koe.metadata.liittyyEnnakkovalmistautumista),
            ohjeetEnnakkovalmistautumiseen = toKielistettyMap(koe.metadata.ohjeetEnnakkovalmistautumiseen),
            erityisjarjestelytMahdollisia = Option.apply(koe.metadata.erityisjarjestelytMahdollisia),
            ohjeetErityisjarjestelyihin = toKielistettyMap(koe.metadata.ohjeetErityisjarjestelyihin)
          )
        ),
        tilaisuudet = koe.tilaisuudet.map(tilaisuus => {
          ValintakoetilaisuusIndexed(
            osoite = Option.apply(
              OsoiteIndexed(
                osoite = toKielistettyMap(tilaisuus.osoite.osoite),
                postinumero = Option.apply(KoodiUri(tilaisuus.osoite.postinumeroKoodiUri))
              )
            ),
            aika = Option.apply(
              Ajanjakso(
                alkaa = if(tilaisuus.aika != null) LocalDateTime.parse(tilaisuus.aika.alkaa) else null,
                paattyy = if(tilaisuus.aika != null) Option.apply(LocalDateTime.parse(tilaisuus.aika.paattyy)) else null
              )
            ),
            lisatietoja = toKielistettyMap(tilaisuus.lisatietoja),
            jarjestamispaikka = toKielistettyMap(tilaisuus.jarjestamispaikka)
          )
        })
      )
    })
    else List.empty

  }
  def getLiitteet(liitteet: List[LiiteES]): List[LiiteIndexed] = {
    if(liitteet != null) liitteet
      .map(l => {
        LiiteIndexed(
          id = Option.apply(UUID.fromString(l.id)),
          tyyppi = if(l.tyyppi != null) Option.apply(KoodiUri(l.tyyppi.koodiUri)) else null,
          nimi = toKielistettyMap(l.nimi),
          kuvaus = toKielistettyMap(l.kuvaus),
          toimitusaika = Option.apply(parseLocalDateTime(l.toimitusaika)),
          toimitustapa = if(l.toimitustapa != null) Option.apply(LiitteenToimitustapa.withName(l.toimitustapa)) else null,
          if (l.toimitusosoite != null)
            Option.apply(
              LiitteenToimitusosoiteIndexed(
                OsoiteIndexed(
                  osoite = toKielistettyMap(l.toimitusosoite.osoite.osoite),
                  postinumero = Option.apply(KoodiUri(l.toimitusosoite.osoite.postinumeroKoodiUri))
                ),
                l.toimitusosoite.sahkoposti,
                l.toimitusosoite.verkkosivu
              )
            )
          else null
        )
      })
      .toList
    else List.empty
  }
  def getOsoite(liitteenToimitusosoite: LiitteenToimitusosoiteES): Option[LiitteenToimitusosoiteIndexed] = {
    if(liitteenToimitusosoite != null) {
      Option.apply(LiitteenToimitusosoiteIndexed(
            OsoiteIndexed(
              toKielistettyMap(liitteenToimitusosoite.osoite.osoite),
              Option.apply(KoodiUri(liitteenToimitusosoite.osoite.postinumeroKoodiUri))
            ),
            liitteenToimitusosoite.sahkoposti,
            liitteenToimitusosoite.verkkosivu
          ))

    }
    None

  }

  def getOsoiteIndexed(osoiteEs: OsoiteES): OsoiteIndexed = {
    OsoiteIndexed(
      osoite = toKielistettyMap(osoiteEs.osoite),
      Option.apply(KoodiUri(osoiteEs.postinumeroKoodiUri))
    )

  }
  def getUUid(valintaperuste: Map[String, Object]): Option[UuidObject] = {
    Option.apply(UuidObject(UUID.fromString(valintaperuste.get("id").get.toString)))
    val uuid =
      if (valintaperuste != null) Option.apply(UuidObject(UUID.fromString(valintaperuste.get("id").get.toString)))
      else None
    uuid
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
    muokkaaja = if(muokkaaja != null) muokkaaja.oid else null,
    metadata = metadata.map(_.toHakukohdeMetadata),
    organisaatioOid = if(organisaatio != null) organisaatio.oid else null,
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
    modified: Option[Modified] ,
    toteutus: Option[Tarjoajat],
    johtaaTutkintoon: Option[Boolean],
    opetuskieliKoodiUrit: Seq[String],
    koulutusasteKoodiUrit: Seq[String],
    hakutapaKoodiUri: Option[String],
    paateltyAlkamiskausi: Option[PaateltyAlkamiskausi]
)
