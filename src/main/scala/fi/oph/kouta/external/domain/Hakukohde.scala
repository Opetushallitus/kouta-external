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
    |          example: "1.2.246.562.20.00000000000000000009"
    |        externalId:
    |          type: string
    |          description: Ulkoinen tunniste jota voidaan käyttää Kouta lomakkeiden mäppäykseen oppilaitosten omien tietojärjestelmien kanssa
    |        toteutusOid:
    |          type: string
    |          description: Hakukohteeseen liitetyn toteutuksen yksilöivä tunniste.
    |          example: "1.2.246.562.17.00000000000000000009"
    |        hakuOid:
    |          type: string
    |          description: Hakukohteeseen liitetyn haun yksilöivä tunniste.
    |          example: "1.2.246.562.29.00000000000000000009"
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |          description: Haun julkaisutila. Jos hakukohde on julkaistu, se näkyy oppijalle Opintopolussa.
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
    |          description: Hakulomakkeen tyyppi. Kertoo, käytetäänkö Atarun (hakemuspalvelun) hakulomaketta, muuta hakulomaketta
    |            (jolloin voidaan lisätä hakulomakkeeseen linkki) tai onko niin, ettei sähkököistä hakulomaketta ole lainkaan, jolloin sille olisi hyvä lisätä kuvaus.
    |          example: "ataru"
    |          enum:
    |            - ataru
    |            - ei sähköistä
    |            - muu
    |        hakulomakeAtaruId:
    |          type: string
    |          description: Hakulomakkeen yksilöivä tunniste, jos käytössä on Atarun (hakemuspalvelun) hakulomake
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        hakulomakeKuvaus:
    |          type: object
    |          description: Hakulomakkeen kuvausteksti eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        hakulomakeLinkki:
    |          type: object
    |          description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |          $ref: '#/components/schemas/Linkki'
    |        kaytetaanHaunHakulomaketta:
    |          type: boolean
    |          description: Käytetäänkö haun hakulomaketta vai onko hakukohteelle määritelty oma hakulomake?
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
    |          description: Hakukohteen muiden pohjakoulutusvaatimusten kuvaus eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        toinenAsteOnkoKaksoistutkinto:
    |          type: boolean
    |          description: Onko hakukohteen toisen asteen koulutuksessa mahdollista suorittaa kaksoistutkinto?
    |        kaytetaanHaunAikataulua:
    |          type: boolean
    |          description: Käytetäänkö haun hakuaikoja vai onko hakukohteelle määritelty omat hakuajat?
    |        hakuajat:
    |          type: array
    |          description: Hakukohteen hakuajat, jos ei käytetä haun hakuaikoja
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
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
    |          description: Jos liitteillä on sama toimitustapa, se ilmoitetaan tässä
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
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille hakukohteen nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        muokkaaja:
    |          type: string
    |          description: Hakukohdetta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        metadata:
    |          type: object
    |          $ref: '#/components/schemas/HakukohdeMetadata'
    |        organisaatioOid:
    |           type: string
    |           description: Hakukohteen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
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
    muokkaaja: UserOid,
    metadata: Option[HakukohdeMetadata],
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified],
) extends PerustiedotWithOid[HakukohdeOid, Hakukohde] {
  override def withMuokkaaja(oid: UserOid): Hakukohde = this.copy(muokkaaja = oid)
}

@SwaggerModel(
  """    HakukohteenLinja:
    |      type: object
    |      properties:
    |        linja:
    |          type: string
    |          description: Linjan koodiUri, tai tyhjä arvo (= yleislinja)
    |          example: lukiopainotukset_0102#1
    |        alinHyvaksyttyKeskiarvo:
    |          type: number
    |          description: Linjan alin hyväksytty keskiarvo
    |          example: 8,2
    |        lisatietoa:
    |          type: object
    |          description: Lisätietoa keskiarvosta
    |          $ref: '#/components/schemas/Kuvaus'
    |""")
case class HakukohteenLinja(linja: Option[String] = None, // NOTE: None tarkoittaa Yleislinjaa
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
      |""")
case class HakukohdeMetadata(valintakokeidenYleiskuvaus: Kielistetty = Map(),
                             kynnysehto: Kielistetty = Map(),
                             valintaperusteenValintakokeidenLisatilaisuudet: Seq[ValintakokeenLisatilaisuudet] = Seq(),
                             koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
                             kaytetaanHaunAlkamiskautta: Option[Boolean] = None,
                             aloituspaikat: Option[Aloituspaikat] = None,
                             // hakukohteenLinja löytyy vain lukiohakukohteilta (pakollisena)
                             hakukohteenLinja: Option[HakukohteenLinja] = None)

