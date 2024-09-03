package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, UserOid}
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
    |          example: 1.2.246.562.29.00000000000000000009
    |        externalId:
    |          type: string
    |          description: Ulkoinen tunniste jota voidaan käyttää Kouta lomakkeiden mäppäykseen oppilaitosten omien tietojärjestelmien kanssa
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |            - poistettu
    |          description: "Haun julkaisutila. Uudet haut luodaan tallennettu-tilaisina (käyttöliittymässä tilana: Luonnos). Kun haku on julkaistu, se näkyy oppijalle Opintopolussa ja on käytettävissä hakemus- ja valintapalveluissa. Tallennetut haut voi muuttaa poistetuiksi, jolloin ne häviävät. Julkaistut haut voi arkistoida, jolloin ne häviävät Opintopolusta näkyvistä. Sallitut tilasiirtymät Poistettu <-- Tallennettu --> Julkaistu <--> Arkistoitu"
    |        nimi:
    |          type: object
    |          description: "Haun Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa."
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        hakutapaKoodiUri:
    |          type: string
    |          description: Haun hakutapa. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/hakutapa/1)
    |          example: hakutapa_03#1
    |        hakukohteenLiittamisenTakaraja:
    |          type: string
    |          format: date-time
    |          description: Viimeinen ajanhetki, jolloin hakuun saa liittää hakukohteen.
    |            Hakukohteita ei saa lisätä enää sen jälkeen, kun haku on käynnissä.
    |          example: 2019-08-23T09:55
    |        hakukohteenMuokkaamisenTakaraja:
    |          type: string
    |          format: date-time
    |          description: Viimeinen ajanhetki, jolloin hakuun liitettyä hakukohdetta on sallittua muokata.
    |            Hakukohteen tietoja ei saa muokata enää sen jälkeen, kun haku on käynnissä.
    |          example: 2019-08-23T09:55
    |        hakukohteenLiittajaOrganisaatiot:
    |          type: array
    |          description: Hakukohteen liittajaorganisaatioiden oidit
    |          items:
    |             type: string
    |          example: [1.2.246.562.10.00101010101, 1.2.246.562.10.00101010102]
    |        ajastettuJulkaisu:
    |          type: string
    |          format: date-time
    |          description: EI KÄYTÖSSÄ. Ajanhetki, jolloin haku ja siihen liittyvät hakukohteet ja koulutukset julkaistaan
    |            automaattisesti Opintopolussa, jos ne eivät vielä ole julkisia
    |          example: 2019-08-23T09:55
    |        alkamiskausiKoodiUri:
    |          type: string
    |          description: Haun koulutusten alkamiskausi. Hakukohteella voi olla eri alkamiskausi kuin haulla.
    |            Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/kausi/1)
    |          example: kausi_k#1
    |        alkamisvuosi:
    |          type: string
    |          description: Haun koulutusten alkamisvuosi. Hakukohteella voi olla eri alkamisvuosi kuin haulla.
    |          example: 2020
    |        kohdejoukkoKoodiUri:
    |          type: string
    |          description: Haun kohdejoukko. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/haunkohdejoukko/1)
    |          example: haunkohdejoukko_17#1
    |        kohdejoukonTarkenneKoodiUri:
    |          type: string
    |          description: Haun kohdejoukon tarkenne. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/haunkohdejoukontarkenne/1)
    |          example: haunkohdejoukontarkenne_1#1
    |        hakulomaketyyppi:
    |          type: string
    |          description: Hakulomakkeen tyyppi. Kertoo, käytetäänkö hakemuspalvelun (ataru) hakulomaketta, muuta hakulomaketta, tai että ei ole sähköistä hakua ollenkaan. Hakemuspalvelun kohdalla seuraavassa kentässä määritellään hakemuspalvelun lomake, mihin haku kuuluu. Muun kohdalla ilmoitetaan hakulomakeLinkki. Jos ei ole sähköistä hakua ilmoitetaan hakulomakeKuvaus.
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
    |          description: Hakulomakkeen kuvausteksti eri kielillä. Kielet on määritetty haun kielivalinnassa. Käytössä vain kun on valittu ei sähköistä hakua.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        hakulomakeLinkki:
    |          type: object
    |          description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa. Käytössä vain muilla kuin Opintopolun hakulomakkeilla.
    |          allOf:
    |            - $ref: '#/components/schemas/Linkki'
    |        hakuvuosi:
    |          type: string
    |          description: Haun hakuajoista päätelty hakuvuosi
    |          example: 2022
    |        hakukausi:
    |          type: string
    |          description: Haun hakuajoista päätelty hakukausi
    |          example: kausi_s#1
    |        hakuajat:
    |          type: array
    |          description: Haun hakuajat. 
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
    |        valintakokeet:
    |          type: array
    |          description: Hakuun liittyvät valintakokeet
    |          items:
    |            $ref: '#/components/schemas/Valintakoe'
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
    |"""
)
case class Haku(
    oid: Option[HakuOid] = None,
    hakukohdeOids: Option[List[HakukohdeOid]] = None,
    externalId: Option[String] = None,
    tila: Julkaisutila = Tallennettu,
    nimi: Kielistetty = Map(),
    hakutapaKoodiUri: Option[String] = None,
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime] = None,
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime] = None,
    hakukohteenLiittajaOrganisaatiot: Seq[OrganisaatioOid] = Seq(),
    ajastettuJulkaisu: Option[LocalDateTime] = None,
    alkamiskausiKoodiUri: Option[String] = None,
    alkamisvuosi: Option[String] = None,
    kohdejoukkoKoodiUri: Option[String] = None,
    kohdejoukonTarkenneKoodiUri: Option[String] = None,
    hakulomaketyyppi: Option[Hakulomaketyyppi] = None,
    hakulomakeAtaruId: Option[UUID] = None,
    hakulomakeKuvaus: Kielistetty = Map(),
    hakulomakeLinkki: Kielistetty = Map(),
    hakuvuosi: Option[Int] = None,
    hakukausi: Option[String] = None,
    metadata: Option[HakuMetadata] = None,
    organisaatioOid: OrganisaatioOid,
    hakuajat: List[Ajanjakso] = List(),
    valintakokeet: Option[List[Valintakoe]] = None,
    muokkaaja: UserOid,
    kielivalinta: Seq[Kieli] = Seq(),
    modified: Option[Modified]
) extends PerustiedotWithOid[HakuOid, Haku] {
  override def withMuokkaaja(muokkaaja: UserOid): Haku = copy(muokkaaja = muokkaaja)
}
