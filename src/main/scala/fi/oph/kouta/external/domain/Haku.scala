package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid, UserOid}
import fi.oph.kouta.domain.{Hakulomaketyyppi, Julkaisutila, Kieli, Modified, Tallennettu}
import fi.oph.kouta.external.swagger.SwaggerModel

import java.time.LocalDateTime
import java.util.UUID

@SwaggerModel(
  """    Haku:
    |      type: object
    |      properties:
    |        oid:
    |          type: string
    |          description: Haun yksilöivä tunniste. Järjestelmän generoima.
    |          example: "1.2.246.562.29.00000000000000000009"
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |          description: Haun julkaisutila. Jos haku on julkaistu, se näkyy oppijalle Opintopolussa.
    |        nimi:
    |          type: object
    |          description: Haun Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        hakutapaKoodiUri:
    |          type: string
    |          description: Haun hakutapa. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/hakutapa/11)
    |          example: hakutapa_03#1
    |        hakukohteenLiittamisenTakaraja:
    |          type: string
    |          format: date-time
    |          description: Viimeinen ajanhetki, jolloin hakuun saa liittää hakukohteen.
    |            Hakukohteita ei saa lisätä enää sen jälkeen, kun haku on käynnissä.
    |          example: 2019-08-23T09:55
    |        hakukohteenMuokkaamiseenTakaraja:
    |          type: string
    |          format: date-time
    |          description: Viimeinen ajanhetki, jolloin hakuun liitettyä hakukohdetta on sallittua muokata.
    |            Hakukohteen tietoja ei saa muokata enää sen jälkeen, kun haku on käynnissä.
    |          example: 2019-08-23T09:55
    |        ajastettuJulkaisu:
    |          type: string
    |          format: date-time
    |          description: Ajanhetki, jolloin haku ja siihen liittyvät hakukohteet ja koulutukset julkaistaan
    |            automaattisesti Opintopolussa, jos ne eivät vielä ole julkisia
    |          example: 2019-08-23T09:55
    |        kohdejoukkoKoodiUri:
    |          type: string
    |          description: Haun kohdejoukko. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/haunkohdejoukko/1)
    |          example: haunkohdejoukko_17#1
    |        kohdejoukonTarkenneKoodiUri:
    |          type: string
    |          description: Haun kohdejoukon tarkenne. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/haunkohdejoukontarkenne/1)
    |          example: haunkohdejoukontarkenne_1#1
    |        hakulomaketyyppi:
    |          type: string
    |          description: Hakulomakkeen tyyppi. Kertoo, käytetäänkö Atarun (hakemuspalvelun) hakulomaketta, muuta hakulomaketta
    |            (jolloin voidaan lisätä hakulomakkeeseen linkki) tai onko niin, ettei sähkököistä hakulomaketta ole lainkaan, jolloin sille olisi hyvä lisätä kuvaus.
    |            Hakukohteella voi olla eri hakulomake kuin haulla.
    |          example: "ataru"
    |          enum:
    |            - ataru
    |            - ei sähköistä
    |            - muu
    |        hakulomakeAtaruId:
    |          type: string
    |          description: Hakulomakkeen yksilöivä tunniste, jos käytössä on Atarun (hakemuspalvelun) hakulomake. Hakukohteella voi olla eri hakulomake kuin haulla.
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        hakulomakeKuvaus:
    |          type: object
    |          description: Hakulomakkeen kuvausteksti eri kielillä. Kielet on määritetty haun kielivalinnassa. Hakukohteella voi olla eri hakulomake kuin haulla.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        hakulomakeLinkki:
    |          type: object
    |          description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa. Hakukohteella voi olla eri hakulomake kuin haulla.
    |          allOf:
    |            - $ref: '#/components/schemas/Linkki'
    |        hakuajat:
    |          type: array
    |          description: Haun hakuajat. Hakukohteella voi olla omat hakuajat.
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
    |        metadata:
    |          type: object
    |          $ref: '#/components/schemas/HakuMetadata'
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille haun nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        muokkaaja:
    |          type: string
    |          description: Hakua viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Haun luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        modified:
    |           type: string
    |           format: date-time
    |           description: Haun viimeisin muokkausaika. Järjestelmän generoima
    |           example: 2019-08-23T09:55
    |""")
case class Haku(
    oid: Option[HakuOid] = None,
    tila: Julkaisutila = Tallennettu,
    nimi: Kielistetty = Map(),
    hakutapaKoodiUri: Option[String] = None,
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime] = None,
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime] = None,
    ajastettuJulkaisu: Option[LocalDateTime] = None,
    kohdejoukkoKoodiUri: Option[String] = None,
    kohdejoukonTarkenneKoodiUri: Option[String] = None,
    hakulomaketyyppi: Option[Hakulomaketyyppi] = None,
    hakulomakeAtaruId: Option[UUID] = None,
    hakulomakeKuvaus: Kielistetty = Map(),
    hakulomakeLinkki: Kielistetty = Map(),
    metadata: Option[HakuMetadata] = None,
    organisaatioOid: OrganisaatioOid,
    hakuajat: List[Ajanjakso] = List(),
    muokkaaja: UserOid,
    kielivalinta: Seq[Kieli] = Seq(),
    modified: Option[Modified]
) extends PerustiedotWithOid[HakuOid, Haku] {
  override def withMuokkaaja(muokkaaja: UserOid): Haku = copy(muokkaaja = muokkaaja)
}
