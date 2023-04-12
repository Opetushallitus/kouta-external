package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.oid._
import fi.oph.kouta.domain.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa, Modified}
import fi.oph.kouta.external.swagger.SwaggerModel

import java.time.LocalDateTime
import java.util.UUID

@SwaggerModel(
  """    Hakukohde:
    |      type: object
    |      properties:
    |        oid:
    |          type: string
    |          description: Hakukohteen yksilöivä tunniste. Järjestelmän generoima.
    |          example: 1.2.246.562.20.00000000000000000009
    |        externalId:
    |          type: string
    |          description: Ulkoinen tunniste jota voidaan käyttää Kouta lomakkeiden mäppäykseen oppilaitosten omien tietojärjestelmien kanssa
    |        toteutusOid:
    |          type: string
    |          description: Hakukohteeseen liitetyn toteutuksen oid.
    |          example: 1.2.246.562.17.00000000000000000009
    |        hakukohderyhmat:
    |          type: array
    |          description: Hakukohderyhmien, joihin hakukohde kuuluu, oidit.
    |          items:
    |            type: string
    |          example:
    |            - 1.2.246.562.28.00000000000000000001
    |            - 1.2.246.562.28.00000000000000000002
    |        hakuOid:
    |          type: string
    |          description: Hakukohteeseen liitetyn haun oid.
    |          example: 1.2.246.562.29.00000000000000000009
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |            - poistettu
    |          description: "Hakukohteen julkaisutila. Uudet hakukohteet luodaan tallennettu-tilaisina (käyttöliittymässä tilana: Luonnos). Kun hakukohde on julkaistu, se näkyy oppijalle Opintopolussa. Tallennetut hakukohteet voi muuttaa poistetuiksi, jolloin ne häviävät. Julkaistut hakukohteet voi arkistoida, jolloin ne häviävät Opintopolusta näkyvistä. Sallitut tilasiirtymät Poistettu <-- Tallennettu --> Julkaistu <--> Arkistoitu"
    |        nimi:
    |          type: object
    |          description: Hakukohteen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Nimi'
    |        tarjoaja:
    |          type: string
    |          description: Hakukohteen järjestyspaikan organisaatioOid
    |          example: 1.2.246.562.10.00101010101
    |        hakulomaketyyppi:
    |          type: string
    |          description: Hakulomakkeen tyyppi. Kertoo, käytetäänkö hakemuspalvelun (ataru) hakulomaketta, muuta hakulomaketta, tai että ei ole sähköistä hakua ollenkaan. Hakemuspalvelun kohdalla seuraavassa kentässä määritellään hakemuspalvelun lomake, mihin haku kuuluu. Muun kohdalla ilmoitetaan hakulomakeLinkki sekä tieto kaytetaanHaunHakulomaketta. Jos käytössä ei ole sähköistä hakua, ilmoitetaan hakulomakeKuvaus.
    |          example: "ataru"
    |          enum:
    |            - ataru
    |            - ei sähköistä
    |            - muu
    |        hakulomakeAtaruId:
    |          type: string
    |          description: Hakulomakkeen yksilöivä tunniste, jos käytössä on hakemuspalvelun (ataru) hakulomake.
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        hakulomakeKuvaus:
    |          type: object
    |          description: Hakulomakkeen kuvausteksti eri kielillä. Kielet on määritetty haun kielivalinnassa. Käytössä vain jos valittu ei sähköistä hakua.
    |          $ref: '#/components/schemas/Kuvaus'
    |        hakulomakeLinkki:
    |          type: object
    |          description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa. Käytössä vain muulla kuin Opintopolun hakulomakkeella.
    |          $ref: '#/components/schemas/Linkki'
    |        kaytetaanHaunHakulomaketta:
    |          type: boolean
    |          description: Käytetäänkö haun hakulomaketta vai onko hakukohteelle määritelty oma hakulomake? Käytössä vain muulla kuin Opintopolun hakulomakkeella.
    |        pohjakoulutusvaatimusKoodiUrit:
    |          type: array
    |          description: Lista hakukohteen pohjakoulutusvaatimuksista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/pohjakoulutusvaatimuskouta/1)
    |          items:
    |            type: string
    |          example:
    |            - pohjakoulutusvaatimuskouta_104#1
    |            - pohjakoulutusvaatimuskouta_109#1
    |        muuPohjakoulutusvaatimus:
    |          type: object
    |          description: Hakukohteen muiden pohjakoulutusvaatimusten vapaa kuvaus eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        toinenAsteOnkoKaksoistutkinto:
    |          type: boolean
    |          description: "Onko hakukohteen toisen asteen koulutuksessa mahdollista suorittaa kaksoistutkinto. Käytössä vain koulutustyypeillä: amm ja lukio"
    |        kaytetaanHaunAikataulua:
    |          type: boolean
    |          description: Käytetäänkö haun hakuaikoja vai onko hakukohteelle määritelty omat hakuajat? Jos false, hakukohteelle voidaan määritellä oma, erillinen hakuaika kentässä hakuajat
    |        valintaperusteId:
    |          type: string
    |          description: Hakukohteeseen liittyvän valintaperustekuvauksen yksilöivä tunniste
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        liitteetOnkoSamaToimitusaika:
    |          type: boolean
    |          description: Onko kaikilla hakukohteen liitteillä sama toimitusaika?
    |        liitteetOnkoSamaToimitusosoite:
    |          type: boolean
    |          description: Onko kaikilla hakukohteen liitteillä sama toimitusosoite?
    |        liitteidenToimitusaika:
    |          type: string
    |          description: Jos liitteillä on sama toimitusaika, se ilmoitetaan tässä
    |          format: date-time
    |          example: 2019-08-23T09:55
    |        liitteidenToimitustapa:
    |          type: string
    |          description: Jos liitteillä on sama toimitustapa, se ilmoitetaan tässä.
    |          example: "hakijapalvelu"
    |          enum:
    |            - hakijapalvelu
    |            - osoite
    |            - lomake
    |        liitteidenToimitusosoite:
    |          type: object
    |          description: Jos liitteillä on sama toimitusosoite, se ilmoitetaan tässä
    |          $ref: '#/components/schemas/LiitteenToimitusosoite'
    |        liitteet:
    |          type: array
    |          description: Hakukohteen liitteet
    |          items:
    |            $ref: '#/components/schemas/Liite'
    |        valintakokeet:
    |          type: array
    |          description: Hakukohteeseen liittyvät valintakokeet
    |          items:
    |            $ref: '#/components/schemas/Valintakoe'
    |        hakuajat:
    |          type: array
    |          description: Hakukohteen hakuajat, jos ei käytetä haun hakuaikoja
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
    |        metadata:
    |          type: object
    |          $ref: '#/components/schemas/HakukohdeMetadata'
    |        muokkaaja:
    |          type: string
    |          description: Hakukohdetta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Hakukohteen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille hakukohteen nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        johtaaTutkintoon:
    |          type: boolean
    |          description: Onko koulutus tutkintoon johtavaa
    |        hakutapaKoodiUri:
    |          type: string
    |          description: Haun hakutapa. Viittaa koodistoon [hakutapa](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/hakutapa)
    |          example: hakutapa_03#1
    |        opetuskieliKoodiUrit:
    |          type: array
    |          description: Lista toteutuksen opetuskielistä. Viittaa koodistoon [oppilaitoksenopetuskieli](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/oppilaitoksenopetuskieli)
    |          items:
    |            type: string
    |            example:
    |              - oppilaitoksenopetuskieli_1#1
    |              - oppilaitoksenopetuskieli_2#1
    |        paateltyAlkamiskausi:
    |          type: object
    |          $ref: '#/components/schemas/PaateltyAlkamiskausi'
    |        koulutusasteKoodiUrit:
    |          type: array
    |          description: 'Koulutuksen koulutusaste. Viittaa koodistoihin [kansallinenkoulutusluokitus2016koulutusastetaso1](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kansallinenkoulutusluokitus2016koulutusastetaso1) ja [kansallinenkoulutusluokitus2016koulutusastetaso2](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kansallinenkoulutusluokitus2016koulutusastetaso2)'
    |          items:
    |            type: string
    |            example:
    |              - kansallinenkoulutusluokitus2016koulutusastetaso1_8#1
    |              - kansallinenkoulutusluokitus2016koulutusastetaso2_31"1
    |        modified:
    |           type: string
    |           format: date-time
    |           description: Hakukohteen viimeisin muokkausaika. Järjestelmän generoima
    |           example: 2019-08-23T09:55:17
    |""")
case class Hakukohde(
    oid: Option[HakukohdeOid],
    externalId: Option[String],
    toteutusOid: ToteutusOid,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    tarjoaja: Option[OrganisaatioOid],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    hakukohderyhmat: Option[Seq[HakukohderyhmaOid]],
    kaytetaanHaunHakulomaketta: Option[Boolean],
    pohjakoulutusvaatimusKoodiUrit: Seq[String],
    pohjakoulutusvaatimusTarkenne: Kielistetty,
    muuPohjakoulutusvaatimus: Kielistetty,
    toinenAsteOnkoKaksoistutkinto: Option[Boolean],
    kaytetaanHaunAikataulua: Option[Boolean],
    valintaperusteId: Option[UUID],
    liitteetOnkoSamaToimitusaika: Option[Boolean],
    liitteetOnkoSamaToimitusosoite: Option[Boolean],
    liitteidenToimitusaika: Option[LocalDateTime],
    liitteidenToimitustapa: Option[LiitteenToimitustapa],
    liitteidenToimitusosoite: Option[LiitteenToimitusosoite],
    liitteet: List[Liite],
    valintakokeet: List[Valintakoe],
    hakuajat: List[Ajanjakso],
    metadata: Option[HakukohdeMetadata],
    muokkaaja: UserOid,
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified],
    johtaaTutkintoon: Option[Boolean],
    hakutapaKoodiUri: Option[String],
    opetuskieliKoodiUrit: Seq[String],
    koulutusasteKoodiUrit: Seq[String],
    paateltyAlkamiskausi: Option[PaateltyAlkamiskausi]
) extends PerustiedotWithOid[HakukohdeOid, Hakukohde]  {
  override def withMuokkaaja(oid: UserOid): Hakukohde = this.copy(muokkaaja = oid)
  def withHakukohderyhmat(oids: Seq[HakukohderyhmaOid]): Hakukohde = this.copy(hakukohderyhmat = Some(oids))
}

@SwaggerModel(
  """    HakukohteenLinja:
    |      type: object
    |      properties:
    |        linja:
    |          type: object
    |          description: Lukion linja, tai tyhjä arvo (= yleislinja).
    |          $ref: '#/components/schemas/Koodi'
    |        alinHyvaksyttyKeskiarvo:
    |          type: number
    |          description: Linjan alin hyväksytty keskiarvo
    |          example: 8,2
    |        lisatietoa:
    |          type: object
    |          description: Lisätietoa keskiarvosta
    |          $ref: '#/components/schemas/Kuvaus'
    |""")
case class HakukohteenLinja(linja: Option[Koodi] = None, // NOTE: None tarkoittaa Yleislinjaa
                            alinHyvaksyttyKeskiarvo: Option[Double] = None,
                            lisatietoa: Kielistetty = Map())

@SwaggerModel(
  """    HakukohdeMetadata:
      |      type: object
      |      properties:
      |        valintakokeidenYleiskuvaus:
      |          type: object
      |          description: Valintakokeiden yleiskuvaus eri kielillä. Kielet on määritetty hakukohteen kielivalinnassa.
      |          $ref: '#/components/schemas/Kuvaus'
      |        kynnysehto:
      |          type: object
      |          description: Hakukohteen kynnysehto eri kielillä. Kielet on määritetty hakukohteen kielivalinnassa.
      |          $ref: '#/components/schemas/Kuvaus'
      |        valintaperusteenValintakokeidenLisatilaisuudet:
      |          type: array
      |          description: Hakukohteeseen liitetyn valintaperusteen valintakokeisiin liitetyt lisätilaisuudet
      |          items:
      |            $ref: '#/components/schemas/ValintakokeenLisatilaisuudet'
      |        koulutuksenAlkamiskausi:
      |          type: object
      |          description: Koulutuksen alkamiskausi
      |          $ref: '#/components/schemas/KoulutuksenAlkamiskausi'
      |        kaytetaanHaunAlkamiskautta:
      |          type: boolean
      |          description: Käytetäänkö haun alkamiskautta ja -vuotta vai onko hakukohteelle määritelty oma alkamisajankohta?
      |        aloituspaikat:
      |          type: object
      |          description: Hakukohteen aloituspaikkojen tiedot
      |          $ref: '#/components/schemas/Aloituspaikat'
      |        hakukohteenLinja:
      |          type: object
      |          description: Hakukohteen haluttu linja, määritelty ainoastaan lukiokohteille.
      |          $ref: '#/components/schemas/HakukohteenLinja'
      |        uudenOpiskelijanUrl:
      |          type: object
      |          description: Uuden opiskelijan ohjeita sisältävän verkkosivun URL
      |          $ref: '#/components/schemas/Linkki'
      |""")
case class HakukohdeMetadata(valintakokeidenYleiskuvaus: Kielistetty = Map(),
                             kynnysehto: Kielistetty = Map(),
                             valintaperusteenValintakokeidenLisatilaisuudet: Seq[ValintakokeenLisatilaisuudet] = Seq(),
                             koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
                             kaytetaanHaunAlkamiskautta: Option[Boolean] = None,
                             aloituspaikat: Option[Aloituspaikat] = None,
                             // hakukohteenLinja löytyy vain lukiohakukohteilta (pakollisena)
                             hakukohteenLinja: Option[HakukohteenLinja] = None,
                             uudenOpiskelijanUrl: Kielistetty = Map())