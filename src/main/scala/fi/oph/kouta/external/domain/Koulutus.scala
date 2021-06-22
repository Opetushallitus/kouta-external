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
    |        johtaaTutkintoon:
    |          type: boolean
    |          description: Onko koulutus tutkintoon johtavaa
    |        koulutustyyppi:
    |          type: string
    |          description: "Koulutuksen tyyppi. Sallitut arvot: 'amm' (ammatillinen), 'yo' (yliopisto), 'lk' (lukio), 'amk' (ammattikorkea), 'amm-tutkinnon-osa', 'amm-osaamisala'"
    |          enum:
    |            - amm
    |            - yo
    |            - amk
    |            - lk
    |            - amm-tutkinnon-osa
    |            - amm-osaamisala
    |    |     example: amm
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
    |          description: Koulutuksen julkaisutila. Jos koulutus on julkaistu, se näkyy oppijalle Opintopolussa.
    |        tarjoajat:
    |          type: array
    |          description: Koulutusta tarjoavien organisaatioiden yksilöivät organisaatio-oidit
    |          items:
    |            type: string
    |          example:
    |            - 1.2.246.562.10.00101010101
    |            - 1.2.246.562.10.00101010102
    |        julkinen:
    |          type: boolean
    |          description: Voivatko muut oppilaitokset käyttää koulutusta
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille koulutuksen nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        nimi:
    |          type: object
    |          description: Koulutuksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        metadata:
    |          type: object
    |          oneOf:
    |            - $ref: '#/components/schemas/YliopistoKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmattikorkeaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenTutkinnonOsaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenOsaamisalaKoulutusMetadata'
    |            - $ref: '#/components/schemas/LukioKoulutusMetadata'
    |             example:
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
    |        sorakuvausId:
    |          type: string
    |          description: Koulutukseen liittyvän SORA-kuvauksen yksilöivä tunniste
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        muokkaaja:
    |          type: string
    |          description: Koulutusta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Koulutuksen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        teemakuva:
    |          type: string
    |          description: Koulutuksen Opintopolussa näytettävän teemakuvan URL.
    |          example: https://konfo-files.opintopolku.fi/koulutus-teema/1.2.246.562.13.00000000000000000009/f4ecc80a-f664-40ef-98e6-eaf8dfa57f6e.png
    |        ePerusteId:
    |          type: number
    |          description: Koulutuksen käyttämän ePerusteen id.
    |          example: 4804100
    |        modified:
    |          type: string
    |          format: date-time
    |          description: Koulutuksen viimeisin muokkausaika. Järjestelmän generoima
    |          example: 2019-08-23T09:55
    |""")
case class Koulutus(
    oid: Option[KoulutusOid],
    johtaaTutkintoon: Boolean,
    koulutustyyppi: Koulutustyyppi,
    koulutuksetKoodiUri: Seq[String],
    tila: Julkaisutila,
    tarjoajat: List[OrganisaatioOid],
    julkinen: Boolean,
    kielivalinta: Seq[Kieli],
    nimi: Kielistetty,
    metadata: Option[KoulutusMetadata],
    sorakuvausId: Option[UUID],
    muokkaaja: UserOid,
    organisaatioOid: OrganisaatioOid,
    teemakuva: Option[String],
    ePerusteId: Option[Long],
    modified: Option[Modified]
) extends PerustiedotWithOid[KoulutusOid, Koulutus]
    with AuthorizableMaybeJulkinen[Koulutus] {
  override def withMuokkaaja(muokkaaja: UserOid): Koulutus = copy(muokkaaja = muokkaaja)
}
