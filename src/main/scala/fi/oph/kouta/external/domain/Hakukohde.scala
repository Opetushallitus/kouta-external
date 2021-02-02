package fi.oph.kouta.external.domain

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.domain.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa}
import fi.oph.kouta.domain.oid._
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    Hakukohde:
    |      type: object
    |      properties:
    |        oid:
    |          type: string
    |          description: Hakukohteen yksilöivä tunniste. Järjestelmän generoima.
    |          example: "1.2.246.562.20.00000000000000000009"
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
    |        alkamiskausiKoodiUri:
    |          type: string
    |          description: Hakukohteen koulutusten alkamiskausi, jos ei käytetä haun alkamiskautta.
    |            Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kausi/1)
    |          example: kausi_k#1
    |        alkamisvuosi:
    |          type: string
    |          description: Hakukohteen koulutusten alkamisvuosi, jos ei käytetä haun alkamisvuotta
    |          example: 2020
    |        kaytetaanHaunAlkamiskautta:
    |          type: boolean
    |          description: Käytetäänkö haun alkamiskautta ja -vuotta vai onko hakukohteelle määritelty oma alkamisajankohta?
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
    |        aloituspaikat:
    |          type: integer
    |          description: Hakukohteen aloituspaikkojen lukumäärä
    |          example: 100
    |        minAloituspaikat:
    |          type: integer
    |          description: Hakukohteen aloituspaikkojen minimimäärä
    |          example: 75
    |        maxAloituspaikat:
    |          type: integer
    |          description: Hakukohteen aloituspaikkojen maksimimäärä
    |          example: 110
    |        ensikertalaisenAloituspaikat:
    |          type: integer
    |          description: Hakukohteen ensikertalaisen aloituspaikkojen lukumäärä
    |          example: 50
    |        minEnsikertalaisenAloituspaikat:
    |          type: integer
    |          description: Hakukohteen ensikertalaisen aloituspaikkojen minimimäärä
    |          example: 45
    |        maxEnsikertalaisenAloituspaikat:
    |          type: integer
    |          description: Hakukohteen ensikertalaisen aloituspaikkojen maksimimäärä
    |          example: 60
    |        pohjakoulutusvaatimusKoodiUrit:
    |          type: array
    |          description: Lista toisen asteen hakukohteen pohjakoulutusvaatimuksista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/pohjakoulutusvaatimustoinenaste/1)
    |          items:
    |            type: string
    |          example:
    |            - pohjakoulutusvaatimustoinenaste_pk#1
    |            - pohjakoulutusvaatimustoinenaste_yo#1
    |        pohjakoulutusvaatimusTarkenne:
    |          type: object
    |          description: Pohjakoulutusvaatimuksen tarkenne eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
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
    |           example: 2019-08-23T09:55
    |        jarjestyspaikkaOid:
    |          type: string
    |          description: Hakukohteen järjestyspaikan organisaatio
    |          example: 1.2.246.562.10.00101010101
    |""")
case class Hakukohde(
    oid: Option[HakukohdeOid],
    toteutusOid: ToteutusOid,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    alkamiskausiKoodiUri: Option[String],
    alkamisvuosi: Option[String],
    kaytetaanHaunAlkamiskautta: Option[Boolean],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    kaytetaanHaunHakulomaketta: Option[Boolean],
    aloituspaikat: Option[Int],
    ensikertalaisenAloituspaikat: Option[Int],
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
    metadata: Option[HakukohdeMetadata] = None,
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    jarjestyspaikkaOid: Option[OrganisaatioOid] = None,
) extends PerustiedotWithOid[HakukohdeOid, Hakukohde] {
  override def withMuokkaaja(oid: UserOid): Hakukohde = this.copy(muokkaaja = oid)
}

@SwaggerModel(
  """    HakukohdeMetadata:
      |      type: object
      |      properties:
      |        valintakokeidenYleiskuvaus:
      |          type: object
      |          description: Valintakokeiden yleiskuvaus eri kielillä. Kielet on määritetty hakukohteen kielivalinnassa.
      |          $ref: '#/components/schemas/Kuvaus'
      |        koulutuksenAlkamiskausi:
      |          type: object
      |          description: Koulutuksen alkamiskausi
      |          $ref: '#/components/schemas/KoulutuksenAlkamiskausi'
      |        kaytetaanHaunAlkamiskautta:
      |          type: boolean
      |          description: Käytetäänkö haun alkamiskautta ja -vuotta vai onko hakukohteelle määritelty oma alkamisajankohta?
    |""")
case class HakukohdeMetadata(valintakokeidenYleiskuvaus: Kielistetty = Map(),
                             koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
                             kaytetaanHaunAlkamiskautta: Option[Boolean] = None)
