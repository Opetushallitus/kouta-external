package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid, UserOid}
import fi.oph.kouta.domain.{Julkaisutila, Kieli, Koulutustyyppi, Modified}
import fi.oph.kouta.external.swagger.SwaggerModel
import fi.oph.kouta.security.AuthorizableMaybeJulkinen

import java.util.UUID

@SwaggerModel(
  """    Koulutus:
    |      type: object
    |      properties:
    |        oid:
    |          type: string
    |          description: Koulutuksen yksilöivä tunniste. Järjestelmän generoima.
    |          example: "1.2.246.562.13.00000000000000000009"
    |        externalId:
    |          type: string
    |          description: Ulkoinen tunniste, jota voidaan käyttää kouta-entiteettien yhdistämiseen oppilaitosten omien tietojärjestelmien kanssa
    |        johtaaTutkintoon:
    |          type: boolean
    |          description: Onko koulutus tutkintoon johtavaa
    |        koulutustyyppi:
    |          type: string
    |          description: "Koulutuksen tyyppi. Sallitut arvot: 'amm' (ammatillinen tutkinto, ml. perustutkinnot, ammatti- ja erikoisammattitutkinnot), 'yo' (yliopistotutkinto), 'lk' (lukio), 'amk' (ammattikorkeakoulututkinto), 'amm-ope-erityisope-ja-opo' (Ammatillinen opettaja-, erityisopettaja ja opinto-ohjaajakoulutus), 'ope-pedag-opinnot' (Opettajien pedagogiset opinnot), 'amm-tutkinnon-osa' (ammatillinen tutkinnon osa), 'amm-osaamisala (ammatillinen osaamisala)', 'amm-muu (muu ammatillinen koulutus)', 'tuva' (tutkintokoulutukseen valmentava koulutus), 'telma' (työhön ja itsenäiseen elämään valmentava koulutus), 'vapaa-sivistystyö-opistovuosi (opistovuosi oppivelvollisille)', 'vapaa-sivistystyo-muu' (muut vapaan sivistystyön koulutukset), 'aikuisten-perusopetus' (aikuisten perusopetus), 'kk-opintojakso', 'kk-opintokokonaisuus', 'erikoislaakari', 'erikoistumiskoulutus', 'muu' (muut koulutukset)"
    |          $ref: '#/components/schemas/Koulutustyyppi'
    |        koulutuksetKoodiUri:
    |          type: array
    |          description: Koulutuksen koodi URIt. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/koulutus/11)
    |          items:
    |            type: string
    |          example:
    |            - koulutus_371101#1
    |            - koulutus_201000#1
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |            - poistettu
    |          description: "Koulutuksen julkaisutila. Uudet koulutukset luodaan tallennettu-tilaisina (käyttöliittymässä tilana: Luonnos). Kun koulutus on julkaistu, se näkyy oppijalle Opintopolussa. Tallennetut koulutukset voi muuttaa poistetuiksi, jolloin ne häviävät. Julkaistut koulutukset voi arkistoida, jolloin ne häviävät Opintopolusta näkyvistä. Sallitut tilasiirtymät Poistettu <-- Tallennettu --> Julkaistu <--> Arkistoitu"
    |        tarjoajat:
    |          type: array
    |          description: Koulutusta tarjoavien organisaatioiden yksilöivät organisaatio-oidit
    |          items:
    |            type: string
    |          example:
    |            - 1.2.246.562.10.00101010101
    |            - 1.2.246.562.10.00101010102
    |        nimi:
    |          type: object
    |          description: Koulutuksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        sorakuvausId:
    |          type: string
    |          description: Koulutukseen liittyvän SORA-kuvauksen yksilöivä tunniste
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        metadata:
    |          type: object
    |          oneOf:
    |            - $ref: '#/components/schemas/YliopistoKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmattikorkeaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmOpeErityisopeJaOpoKoulutusMetadata'
    |            - $ref: '#/components/schemas/OpePedagOpinnotKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenTutkinnonOsaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenOsaamisalaKoulutusMetadata'
    |            - $ref: '#/components/schemas/LukioKoulutusMetadata'
    |            - $ref: '#/components/schemas/TuvaKoulutusMetadata'
    |            - $ref: '#/components/schemas/TelmaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenMuuKoulutusMetadata'
    |            - $ref: '#/components/schemas/VapaaSivistystyoKoulutusMetadata'
    |            - $ref: '#/components/schemas/AikuistenPerusopetusKoulutusMetadata'
    |            - $ref: '#/components/schemas/KkOpintojaksoKoulutusMetadata'
    |            - $ref: '#/components/schemas/ErikoislaakariKoulutusMetadata'
    |            - $ref: '#/components/schemas/KkOpintokokonaisuusKoulutusMetadata'
    |            - $ref: '#/components/schemas/ErikoistumiskoulutusMetadata'
    |          example:
    |            koulutustyyppi: amm
    |            koulutusalaKoodiUrit:
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_054#1
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_055#1
    |            kuvaus:
    |              fi: Suomenkielinen kuvaus
    |              sv: Ruotsinkielinen kuvaus
    |            lisatiedot:
    |              - otsikkoKoodiUri: koulutuksenlisatiedot_03#1
    |                teksti:
    |                  fi: Opintojen suomenkielinen lisätietokuvaus
    |                  sv: Opintojen ruotsinkielinen lisätietokuvaus
    |        julkinen:
    |          type: boolean
    |          description: Parametri, jolla voidaan määritellä, voivatko muut oppilaitokset luoda toteutuksia tälle kyseiselle koulutukselle. Julkisia koulutuksia ovat mm. ammatilliset tutkinnot, korkeakoulujen yhteisesti sovitut koulutukset sekä jotkin muut koulutukset (tuva, telma, opistovuosi, lukio). Älä valitse tätä, jos koulutus ei ole tarkoitettu yhteiskäyttöön ja siitä ei ole sovittu OPH:n kanssa. Julkiset koulutukset näkyvät kaikilla muilla oppilaitoksilla koulutustarjonnan ylläpidon käyttöliittymässä.
    |        muokkaaja:
    |          type: string
    |          description: Koulutusta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.24.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Koulutuksen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joilla koulutuksen tiedot esitetään Opintopolussa. Jos tiettyä kieliversiota ei ole valittu, näytetään kieliversiot järjestyksessä fi->sv->en, en->fi->sv, sv->fi->en.
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        teemakuva:
    |          type: string
    |          description: Koulutuksen Opintopolussa näytettävän teemakuvan URL.
    |          example: https://konfo-files.opintopolku.fi/koulutus-teema/1.2.246.562.13.00000000000000000009/f4ecc80a-f664-40ef-98e6-eaf8dfa57f6e.png
    |        ePerusteId:
    |          type: number
    |          description: Koulutuksen käyttämän ePerusteen id. Huomaa, että tietyissä tapauksissa ePerusteId riippuu käytetystä koulutuksetKoodiUrista (ammatilliset tutkinnot, ammatilliset tutkinnon osat, ammatilliset osaamisalat)
    |          example: 4804100
    |        modified:
    |          type: string
    |          format: date-time
    |          description: Koulutuksen viimeisin muokkausaika. Järjestelmän generoima
    |          example: 2019-08-23T09:55
    |"""
)
case class Koulutus(
    oid: Option[KoulutusOid],
    externalId: Option[String],
    johtaaTutkintoon: Boolean,
    koulutustyyppi: Koulutustyyppi,
    koulutuksetKoodiUri: Seq[String],
    tila: Julkaisutila,
    tarjoajat: List[OrganisaatioOid],
    nimi: Kielistetty,
    sorakuvausId: Option[UUID],
    metadata: Option[KoulutusMetadata],
    julkinen: Boolean,
    muokkaaja: UserOid,
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli],
    teemakuva: Option[String],
    ePerusteId: Option[Long],
    modified: Option[Modified]
) extends PerustiedotWithOid[KoulutusOid, Koulutus]
    with AuthorizableMaybeJulkinen[Koulutus] {
  override def withMuokkaaja(muokkaaja: UserOid): Koulutus = copy(muokkaaja = muokkaaja)
}
