package fi.oph.kouta.external.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonIgnoreProperties, JsonProperty, JsonUnwrapped}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, HakukohderyhmaOid, Oid, OrganisaatioOid, ToteutusOid, UserOid}
import fi.oph.kouta.domain.{oid, _}
import fi.oph.kouta.external.domain.{Ajanjakso, Aloituspaikat, Hakukohde, HakukohdeMetadata, HakukohteenLinja, Kielistetty, PaateltyAlkamiskausi, ValintakokeenLisatilaisuudet}

import java.time.LocalDateTime
import java.util
import java.util.UUID

// Haluttu luokkarakenne
case class Result(oid: Option[Oid], oid2: Option[Oid], oid3: Option[Oid])
case class LiitteenToimitusosoiteIndexedES(osoite: OsoiteIndexed, sahkoposti: Option[String], verkkosivu: Option[String])
case class liiteES(id: String
                  )
case class OsoiteES @JsonCreator()(
                                    @JsonProperty("osoite") osoite:  Map[String, String],
                                    @JsonProperty("postinumeroKoodiUri") postinumeroKoodiUri: String)
case class LiitteenToimitusosoiteES @JsonCreator()(
                                    @JsonProperty("osoite") osoite: OsoiteES,
                                    @JsonProperty("sahkoposti") sahkoposti: Option[String],
                                    @JsonProperty("verkkosivu") verkkosivu: Option[String])
case class LiiteTyyppiES @JsonCreator()(
                                         @JsonProperty("koodiUri") koodiUri: String,
                                         @JsonProperty("nimi") nimi: Map[String, String]
                                       )
case class LiiteES @JsonCreator()(@JsonProperty("id") id: String,
                                  @JsonProperty("tyyppi") tyyppi: LiiteTyyppiES,
                                  @JsonProperty("nimi") nimi: Map[String, String],
                                  @JsonProperty("kuvaus") kuvaus: Map[String, String],
                                  @JsonProperty("toimitusaika") toimitusaika: String,
                                  @JsonProperty("toimitustapa") toimitustapa: String,
                                  @JsonProperty("toimitusosoite") toimitusosoite: LiitteenToimitusosoiteES
                                 )
case class LiitteetToimitusosoite @JsonCreator()(
                                         @JsonProperty("osoite") osoite: LiitteetToimitusosoiteOsoite,
                                         @JsonProperty("sahkoposti") sahkoposti: Option[String],
                                         @JsonProperty("verkkosivu") verkkosivu: Option[String]
                                       )
case class LiitteetToimitusosoiteOsoite @JsonCreator()(
                                    @JsonProperty("osoite") osoite: Map[String, String],
                                    @JsonProperty("postinumero") postinumero: String
                                                )
/*case class LiitteetToimitusosoiteOsoitePostinumero @JsonCreator()(
                                                                   @JsonProperty("osoite") osoite: Map[String, String]
                                                                 )*/


case class ValintakoeES @JsonCreator()(
                                      @JsonProperty("id") id: String, //id: Option[UUID],
                                      @JsonProperty("tyyppi") tyyppi: ValintakoeTyyppi, // tyyppi: Option[KoodiUri],
                                      @JsonProperty("nimi") nimi : Map[String, String], // nimi: Kielistetty,
                                      @JsonProperty("metadata") metadata: ValintaKoeMetadata, // Option[ValintaKoeMetadataIndexed],
                                      @JsonProperty("tilaisuudet") tilaisuudet: List[ValintakoeTilaisuus] // List[ValintakoetilaisuusIndexed]
                                    )

// ESResult(List(Valintakoe(List(Map(aika -> Map(alkaa -> 2023-07-17T09:00, formatoituAlkaa -> Map(en -> Jul. 17, 2023 at 09:00 AM UTC+3, fi -> 17.7.2023 klo 09:00, sv -> 17.7.2023 kl. 09:00), formatoituPaattyy -> Map(en -> Jul. 17, 2023 at 03:00 PM UTC+3, fi -> 17.7.2023 klo 15:00, sv -> 17.7.2023 kl. 15:00), paattyy -> 2023-07-17T15:00), jarjestamispaikka -> Map(fi -> Rauma), lisatietoja -> Map(), osoite -> Map(osoite -> Map(fi -> Steniuksenkatu 8), postinumero -> Map(koodiUri -> posti_26100#2, nimi -> Map(fi -> RAUMA, sv -> RAUMA))))))))
case class AikaJakso @JsonCreator()(
                                                   @JsonProperty("alkaa") alkaa: String,
                                                   @JsonProperty("formatoituAlkaa") formatoituAlkaa : Map[String, String],
                                                   @JsonProperty("formatoituPaattyy") formatoituPaattyy : Map[String, String],
                                                   @JsonProperty("paattyy") paattyy: String
                                                 )
case class ValintakoeTilaisuus @JsonCreator()(
                                               @JsonProperty("aika") aika: AikaJakso,
                                               @JsonProperty("jarjestamispaikka") jarjestamispaikka: Map[String, String],
                                               @JsonProperty("lisatietoja") lisatietoja: Map[String, String],
                                               @JsonProperty("osoite") osoite: OsoiteES

                                                 )
case class ValintaKoeMetadata @JsonCreator()(
                                          @JsonProperty("liittyyEnnakkovalmistautumista") liittyyEnnakkovalmistautumista: Boolean,
                                          @JsonProperty("ohjeetEnnakkovalmistautumiseen") ohjeetEnnakkovalmistautumiseen: Map[String, String],
                                          @JsonProperty("erityisjarjestelytMahdollisia") erityisjarjestelytMahdollisia: Boolean,
                                          @JsonProperty("ohjeetErityisjarjestelyihin") ohjeetErityisjarjestelyihin: Map[String, String],
                                          @JsonProperty("tietoja") tietoja: Map[String, String],
                                          @JsonProperty("vahimmaispisteet") vahimmaispisteet: Double)
case class ValintakoeTyyppi @JsonCreator ()(
                                             @JsonProperty("koodiUri") koodiUri: String,
                                             @JsonProperty("nimi") nimi : Map[String, String], // nimi: Kielistetty,
                                           )
case class MuokkaajaES @JsonCreator()(
                                       @JsonProperty("nimi") nimi: String,
                                       @JsonProperty("oid") oid: String)

case class AloituspaikatES @JsonCreator ()(
    @JsonProperty("kuvaus") kuvaus: Map[String, String],
    @JsonProperty("lukumaara") lukumaara: Int
                                        )
case class MetadataES @JsonCreator()(
    @JsonProperty("aloituspaikat") aloituspaikat: AloituspaikatES,
    @JsonProperty("isMuokkaajaOphVirkailija") isMuokkaajaOphVirkailija : Boolean,
    @JsonProperty("kaytetaanHaunAlkamiskautta") kaytetaanHaunAlkamiskautta : Boolean,
    @JsonProperty("kynnysehto") kynnysehto : Map[String, String],
    @JsonProperty("uudenOpiskelijanUrl") uudenOpiskelijanUrl : Map[String, String],
    @JsonProperty("valintakokeidenYleiskuvaus") valintakokeidenYleiskuvaus: Map[String, String],
   @JsonProperty("valintaperusteenValintakokeidenLisatilaisuudet") valintaperusteenValintakokeidenLisatilaisuudet: Seq[ValintakoeLisatilaisuusIndexedES])



case class ValintakoeLisatilaisuusIndexedES @JsonCreator()(
    @JsonProperty("id") id : String,
    @JsonProperty("tilaisuudet") tilaisuudet : Seq[ValintakoetilaisuusES]
                                                               )
case class ValintakoetilaisuusES @JsonCreator()(
    @JsonProperty("aika") aika : AikaJakso,
    @JsonProperty("jarjestamispaikka") jarjestamispaikka : Map[String, String],
    @JsonProperty("lisatietoja") lisatietoja : Map[String, String],
    @JsonProperty("osoite") osoite : OsoiteES)
case class OrganisaatioES @JsonCreator()(
    @JsonProperty("oid") oid : String)
case class TarjoajaES @JsonCreator()(
@JsonProperty("oid") a : String
                                    )
case class ToteutusES @JsonCreator()(
    @JsonProperty("tarjoajat") tarjoajat : List[TarjoajaES]
                                    )
case class PaateltyAlkamiskausiES @JsonCreator()(
          @JsonProperty("alkamiskausityyppi") alkamiskausityyppi: String,
          @JsonProperty("kausiUri") kausiUri: String,
          @JsonProperty("source") source: String,
          @JsonProperty("vuosi") vuosi: String,
                                                )
case class ESResult @JsonCreator()(
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
                    @JsonProperty("valintakokeet") valintakokeet:List[ValintakoeES],
                    @JsonProperty("hakuajat") hakuajat: List[AikaJakso],
                    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
                    @JsonProperty("metadata") metadata: MetadataES,
                    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
                    @JsonProperty("kielivalinta") kielivalinta: List[String],
                    @JsonProperty("modified") modified : String,
                    @JsonProperty("toteutus") toteutus: ToteutusES,
                    @JsonProperty("johtaaTutkintoon") johtaaTutkintoon: Boolean,
                    @JsonProperty("opetuskieliKoodiUrit") opetuskieliKoodiUrit: Seq[String],
                    @JsonProperty("koulutusasteKoodiUrit") koulutusasteKoodiUrit: Seq[String],
                    @JsonProperty("hakutapaKoodiUri")  hakutapaKoodiUri: String,
                    @JsonProperty("paateltyAlkamiskausi") paateltyAlkamiskausi: PaateltyAlkamiskausiES
  ) {

  def toResult(): HakukohdeIndexedTest = {
    HakukohdeIndexedTest(

      /*
      Tästä alas toimii
      Option.apply(oid).map(oid => HakukohdeOid(oid)),
      Option.apply(externalId),
      ToteutusOid(toteutusOid),
      HakuOid(hakuOid),
      Julkaisutila.withName(tila),
      nimi = Map(
        En -> nimi.getOrElse("en", None).toString,
        Fi -> nimi.getOrElse("fi", None).toString,
        Sv -> nimi.getOrElse("sv", None).toString
    ),
      Option.apply(Organisaatio(OrganisaatioOid(organisaatio.get("oid").get.toString))),
      Option.apply(hakulomaketyyppi).map(hakulomaketyyppi => Hakulomaketyyppi.withName(hakulomaketyyppi)),
      Option.apply(hakulomakeAtaruId).map(hakulomakeAtaruId => UUID.fromString(hakulomakeAtaruId)),
      hakulomakeKuvaus = Map(
        En -> hakulomakeKuvaus.getOrElse("en", None).toString,
        Fi -> hakulomakeKuvaus.getOrElse("fi", None).toString,
        Sv -> hakulomakeKuvaus.getOrElse("sv", None).toString
      ),
      hakulomakeLinkki = Map(
        En -> hakulomakeLinkki.getOrElse("en", None).toString,
        Fi -> hakulomakeLinkki.getOrElse("fi", None).toString,
        Sv -> hakulomakeLinkki.getOrElse("sv", None).toString
      ),
      Option.apply(kaytetaanHaunHakulomaketta),
      pohjakoulutusvaatimus = pohjakoulutusvaatimus.map(p => {
         KoodiUri(p.get("koodiUri").toString)
      }).toSeq,
      pohjakoulutusvaatimusTarkenne = Map(
        En -> pohjakoulutusvaatimusTarkenne.getOrElse("en", None).toString,
        Fi -> pohjakoulutusvaatimusTarkenne.getOrElse("fi", None).toString,
        Sv -> pohjakoulutusvaatimusTarkenne.getOrElse("sv", None).toString
      ),
      muuPohjakoulutusvaatimus = Map(
        En -> muuPohjakoulutusvaatimus.getOrElse("en", None).toString,
        Fi -> muuPohjakoulutusvaatimus.getOrElse("fi", None).toString,
        Sv -> muuPohjakoulutusvaatimus.getOrElse("sv", None).toString
      ),
      Option.apply(toinenAsteOnkoKaksoistutkinto),
      Option.apply(kaytetaanHaunAikataulua),
      //Option.apply(UuidObject(UUID.fromString(valintaperuste.get("id").get.toString))),
      if (valintaperuste != null) Option.apply(UuidObject(UUID.fromString(valintaperuste.get("id").get.toString))) else null,
      Option.apply(liitteetOnkoSamaToimitusaika),
      Option.apply(liitteetOnkoSamaToimitusosoite),
      if(liitteidenToimitusaika != null) {
        Option.apply(LocalDateTime.parse(liitteidenToimitusaika))
      } else {
        None
      },
            Option.apply(LiitteenToimitustapa.withName(liitteidenToimitustapa)),

      getOsoite(liitteidenToimitusosoiteES),
      liitteet = getLiitteet(liitteet),
      getValintakokeet(valintakokeet),
     hakuajat.map(hakuaika => {
        Ajanjakso(LocalDateTime.parse(hakuaika.alkaa), Option.apply(LocalDateTime.parse(hakuaika.paattyy)))
      }),
     Muokkaaja(UserOid(muokkaaja.oid))


      Tästä ylös toimii

      */

      //metadata: Option[HakukohdeMetadataIndexed],
      getHakukohdeMetadataIndexed(metadata)

      /*
      x

      x
      x

      organisaatio: Organisaatio,
      kielivalinta: Seq[Kieli],
      modified: Option[Modified],
      toteutus: Option[Tarjoajat],
      johtaaTutkintoon: Option[Boolean],
      opetuskieliKoodiUrit: Seq[String],
      koulutusasteKoodiUrit: Seq[String],
      hakutapaKoodiUri: Option[String],
      paateltyAlkamiskausi: Option[PaateltyAlkamiskausi]
       */
    )
  }
def getHakukohdeMetadataIndexed(s: MetadataES): HakukohdeMetadataIndexed = {
  HakukohdeMetadataIndexed(
    valintakokeidenYleiskuvaus = Map(
      En -> s.valintakokeidenYleiskuvaus.getOrElse("en", None).toString,
      Fi -> s.valintakokeidenYleiskuvaus.getOrElse("fi", None).toString,
      Sv -> s.valintakokeidenYleiskuvaus.getOrElse("sv", None).toString
    ),
    kynnysehto = Map(
      En -> s.kynnysehto.getOrElse("en", None).toString,
      Fi -> s.kynnysehto.getOrElse("fi", None).toString,
      Sv -> s.kynnysehto.getOrElse("sv", None).toString
    ),
    valintaperusteenValintakokeidenLisatilaisuudet = s.valintaperusteenValintakokeidenLisatilaisuudet.map(tilaisuus => {
      ValintakoetilaisuusIndexed(tilaisuus.tilaisuudet, tilaisuus.tilaisuudet.a)
    }) ,



    koulutuksenAlkamiskausi = ???, kaytetaanHaunAlkamiskautta = ???, aloituspaikat = ???, hakukohteenLinja = ???)
}
  def getValintakokeet(valintakoeList: List[ValintakoeES]) : List[ValintakoeIndexed] = {
  valintakoeList.map(koe => {
    ValintakoeIndexed(
      id = Option.apply(UUID.fromString(koe.id)),
      tyyppi = Option.apply(KoodiUri(koe.tyyppi.koodiUri)),
      nimi = Map(
        En -> koe.nimi.getOrElse("en", None).toString,
        Fi -> koe.nimi.getOrElse("fi", None).toString,
        Sv -> koe.nimi.getOrElse("sv", None).toString
      ),

      metadata = Option.apply(ValintaKoeMetadataIndexed(tietoja = Map(
        En -> koe.metadata.tietoja.getOrElse("en", None).toString,
        Fi -> koe.metadata.tietoja.getOrElse("fi", None).toString,
        Sv -> koe.metadata.tietoja.getOrElse("sv", None).toString
      ),
        vahimmaispisteet = Option.apply(koe.metadata.vahimmaispisteet),
        liittyyEnnakkovalmistautumista = Option.apply(koe.metadata.liittyyEnnakkovalmistautumista),
        ohjeetEnnakkovalmistautumiseen = Map(
          En -> koe.metadata.ohjeetEnnakkovalmistautumiseen.getOrElse("en", None).toString,
          Fi -> koe.metadata.ohjeetEnnakkovalmistautumiseen.getOrElse("fi", None).toString,
          Sv -> koe.metadata.ohjeetEnnakkovalmistautumiseen.getOrElse("sv", None).toString
        ),
        erityisjarjestelytMahdollisia = Option.apply(koe.metadata.erityisjarjestelytMahdollisia),
        ohjeetErityisjarjestelyihin = Map(
        En -> koe.metadata.ohjeetErityisjarjestelyihin.getOrElse("en", None).toString,
        Fi -> koe.metadata.ohjeetErityisjarjestelyihin.getOrElse("fi", None).toString,
        Sv -> koe.metadata.ohjeetErityisjarjestelyihin.getOrElse("sv", None).toString
      )

      )),
      tilaisuudet = koe.tilaisuudet.map(
        tilaisuus => {
          ValintakoetilaisuusIndexed(
            osoite = Option.apply(OsoiteIndexed(
              osoite = Map(
                En -> tilaisuus.osoite.osoite.getOrElse("en", None).toString,
                Fi -> tilaisuus.osoite.osoite.getOrElse("fi", None).toString,
                Sv -> tilaisuus.osoite.osoite.getOrElse("sv", None).toString
              ),
              postinumero = Option.apply(KoodiUri(tilaisuus.osoite.postinumeroKoodiUri))
            )),
            aika = Option.apply(Ajanjakso(alkaa = LocalDateTime.parse(tilaisuus.aika.alkaa),
            paattyy = Option.apply(LocalDateTime.parse(tilaisuus.aika.paattyy))
            )),
            lisatietoja =
              Map(
                En -> tilaisuus.lisatietoja.getOrElse("en", None).toString,
                Fi -> tilaisuus.lisatietoja.getOrElse("fi", None).toString,
                Sv -> tilaisuus.lisatietoja.getOrElse("sv", None).toString
              ),
            jarjestamispaikka = Map(
              En -> tilaisuus.jarjestamispaikka.getOrElse("en", None).toString,
              Fi -> tilaisuus.jarjestamispaikka.getOrElse("fi", None).toString,
              Sv -> tilaisuus.jarjestamispaikka.getOrElse("sv", None).toString
            ))
        }
      ))
  })

}
  def getLiitteet(liitteet: List[LiiteES]) : List[LiiteIndexed] = {
    println("getting liitteet!")
liitteet.map(l => {
LiiteIndexed(
  id = Option.apply(UUID.fromString(l.id)),
  tyyppi = Option.apply(KoodiUri(l.tyyppi.koodiUri)),
  nimi = Map(
    En -> l.nimi.getOrElse("en", None).toString,
    Fi -> l.nimi.getOrElse("fi", None).toString,
    Sv -> l.nimi.getOrElse("sv", None).toString
  ),
  kuvaus = Map(
  En -> l.kuvaus.getOrElse("en", None).toString,
  Fi -> l.kuvaus.getOrElse("fi", None).toString,
  Sv -> l.kuvaus.getOrElse("sv", None).toString
),
  toimitusaika = Option.apply(LocalDateTime.parse(l.toimitusaika)),
  toimitustapa = Option.apply(LiitteenToimitustapa.withName(l.toimitustapa)),
  if (l.toimitusosoite != null)
   Option.apply(LiitteenToimitusosoiteIndexed(
    OsoiteIndexed(osoite = Map(
      En -> l.toimitusosoite.osoite.osoite.getOrElse("en", None).toString,
      Fi -> l.toimitusosoite.osoite.osoite.getOrElse("fi", None).toString,
      Sv -> l.toimitusosoite.osoite.osoite.getOrElse("sv", None).toString
    ), postinumero = Option.apply(KoodiUri(l.toimitusosoite.osoite.postinumeroKoodiUri))), l.toimitusosoite.sahkoposti, l.toimitusosoite.verkkosivu))
  else null
)
}).toList
  }
  def getOsoite(liitteenToimitusosoite: LiitteenToimitusosoiteES) : Option[LiitteenToimitusosoiteIndexed] = {

//  liitteenToimitusosoite.osoite.osoite.

  //var osoiteen = liitteenToimitusosoite.osoite.osoite.getOrElse("en", None).toString
  var osoiteen = liitteenToimitusosoite.osoite.osoite.getOrElse("en", None).toString
  var osoitefi = liitteenToimitusosoite.osoite.osoite.getOrElse("fi", None).toString
  var osoitesv = liitteenToimitusosoite.osoite.osoite.getOrElse("sv", None).toString

  val postinumero = liitteenToimitusosoite.osoite.postinumeroKoodiUri
  Option.apply(liitteenToimitusosoite).map( liitteenToimitusosoite =>
    LiitteenToimitusosoiteIndexed(OsoiteIndexed(
      //Map.empty,
      Map(
        En -> osoiteen, //hakulomakeKuvaus.getOrElse("en", None).toString,
        Fi -> osoitefi, //hakulomakeKuvaus.getOrElse("fi", None).toString,
        Sv -> osoitesv //hakulomakeKuvaus.getOrElse("sv", None).toString
      ),
      Option.apply(KoodiUri(postinumero))
    ),
    liitteenToimitusosoite.sahkoposti,
    liitteenToimitusosoite.verkkosivu
  ))


}
  def getUUid(valintaperuste: Map[String, Object]): Option[UuidObject] = {
    Option.apply(UuidObject(UUID.fromString(valintaperuste.get("id").get.toString)))
    val uuid = if (valintaperuste != null) Option.apply(UuidObject(UUID.fromString(valintaperuste.get("id").get.toString))) else null
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
//  @JsonProperty
/*  def this() = this(
    None, None, ToteutusOid(""), HakuOid(""),
    Poistettu, Map.empty, None, None, None, Map.empty, Map.empty, None, Seq.empty, Map.empty, Map.empty,
   None, None, None, None, None, None, None, None, List.empty, List.empty, List.empty, Muokkaaja(UserOid("")), None, Organisaatio(OrganisaatioOid("")),
    Seq.empty, None, None, None, Seq.empty, Seq.empty, None, None)*/
  def toHakukohde(hakukohderyhmat: Option[Seq[HakukohderyhmaOid]]): Hakukohde = Hakukohde(
    oid = oid,
    externalId = externalId,
    toteutusOid = toteutusOid,
    hakuOid = hakuOid,
    tila = tila,
    nimi = Map.empty,
    tarjoaja = jarjestyspaikka.map(_.oid),
    hakulomaketyyppi = hakulomaketyyppi,
    hakulomakeAtaruId = hakulomakeAtaruId,
    hakulomakeKuvaus = Map.empty,
    hakulomakeLinkki = Map.empty,
    hakukohderyhmat = hakukohderyhmat,
    kaytetaanHaunHakulomaketta = kaytetaanHaunHakulomaketta,
    pohjakoulutusvaatimusKoodiUrit = pohjakoulutusvaatimus.map(_.koodiUri),
    pohjakoulutusvaatimusTarkenne = Map.empty,
    muuPohjakoulutusvaatimus = Map.empty,
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
    //metadata = metadata.map(_.toHakukohdeMetadata),
    metadata = Option.empty,
    organisaatioOid = organisaatio.oid,
    kielivalinta = Seq.empty,
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

case class HakukohdeMetadataIndexed(valintakokeidenYleiskuvaus: Kielistetty,
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
    valintaperusteenValintakokeidenLisatilaisuudet = valintaperusteenValintakokeidenLisatilaisuudet.map(_.toValintakokeenLisatilaisuudet),
    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.map(_.toKoulutuksenAlkamiskausi),
    kaytetaanHaunAlkamiskautta = kaytetaanHaunAlkamiskautta,
    aloituspaikat = aloituspaikat,
    hakukohteenLinja = hakukohteenLinja
  )
}

case class ValintakokeenLisatilaisuudetIndexed(id: Option[UUID],
                                               tilaisuudet: Seq[ValintakoetilaisuusIndexed] = Seq()
                                              ) {
  def toValintakokeenLisatilaisuudet: ValintakokeenLisatilaisuudet = ValintakokeenLisatilaisuudet(
    id = id,
    tilaisuudet = tilaisuudet.map(_.toValintakoetilaisuus)
  )
}

case class HakukohdeIndexedTest(

                                 /*
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
                                  */
                                        metadata: Option[HakukohdeMetadataIndexed],

    /*// Ei mapattu


    organisaatio: Organisaatio,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified],
    toteutus: Option[Tarjoajat],
    johtaaTutkintoon: Option[Boolean],
    opetuskieliKoodiUrit: Seq[String],
    koulutusasteKoodiUrit: Seq[String],
    hakutapaKoodiUri: Option[String],
    paateltyAlkamiskausi: Option[PaateltyAlkamiskausi]
      */
                               )


