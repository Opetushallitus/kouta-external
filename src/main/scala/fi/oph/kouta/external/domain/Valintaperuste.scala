package fi.oph.kouta.external.domain

import java.util.UUID

import fi.oph.kouta.domain.oid.{OrganisaatioOid, UserOid}
import fi.oph.kouta.domain.{Julkaisutila, Kieli, Koulutustyyppi, Modified}
import fi.oph.kouta.external.swagger.SwaggerModel
import fi.oph.kouta.security.AuthorizableMaybeJulkinen

@SwaggerModel(
  """    Valintaperuste:
    |      type: object
    |      properties:
    |        id:
    |          type: string
    |          description: Valintaperustekuvauksen yksilöivä tunniste. Järjestelmän generoima.
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
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
    |          description: Valintaperustekuvauksen julkaisutila. Jos kuvaus on julkaistu, se näkyy oppijalle Opintopolussa.
    |        esikatselu:
    |          type: boolean
    |          description: Onko koulutus nähtävissä esikatselussa
    |        koulutustyyppi:
    |          type: string
    |          description: Minkä tyyppisille koulutuksille valintaperustekuvaus on tarkoitettu käytettäväksi?
    |          $ref: '#/components/schemas/Koulutustyyppi'
    |          example: amm
    |        hakutapaKoodiUri:
    |          type: string
    |          description: Valintaperustekuvaukseen liittyvä hakutapa. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/hakutapa/11)
    |          example: hakutapa_03#1
    |        kohdejoukkoKoodiUri:
    |          type: string
    |          description: Valintaperustekuvaukseen liittyvä kohdejoukko. Valintaperusteen ja siihen hakukohteen kautta liittyvän haun kohdejoukon tulee olla sama. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/haunkohdejoukko/1)
    |          example: haunkohdejoukko_17#1
    |        julkinen:
    |          type: boolean
    |          description: Voivatko muut oppilaitokset käyttää valintaperustekuvausta
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille valintaperustekuvauksen nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        nimi:
    |          type: object
    |          description: Valintaperustekuvauksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        valintakokeet:
    |          type: array
    |          description: Hakuun liittyvät valintakokeet
    |          items:
    |            $ref: '#/components/schemas/Valintakoe'
    |        metadata:
    |          type: object
    |          $ref: '#/components/schemas/ValintaperusteMetadata'
    |          example:
    |            tyyppi: amm
    |            valintatavat:
    |              - valintatapaKoodiUri: valintatapajono_tv#1
    |                kuvaus:
    |                  fi: Valintatavan suomenkielinen kuvaus
    |                  sv: Valintatavan ruotsinkielinen kuvaus
    |                sisalto:
    |                  - tyyppi: teksti
    |                    data:
    |                      fi: Suomenkielinen sisältöteksti
    |                      sv: Ruotsinkielinen sisältöteksti
    |                  - tyyppi: taulukko
    |                    data:
    |                      nimi:
    |                        fi: Taulukon nimi suomeksi
    |                        sv: Taulukon nimi ruotsiksi
    |                      rows:
    |                        - index: 0
    |                          isHeader: true
    |                          columns:
    |                            - index: 0
    |                              text:
    |                                fi: Otsikko suomeksi
    |                                sv: Otsikko ruotsiksi
    |                kaytaMuuntotaulukkoa: true
    |                kynnysehto:
    |                  fi: Kynnysehto suomeksi
    |                  sv: Kynnysehto ruotsiksi
    |                enimmaispisteet: 18.1
    |                vahimmaispisteet: 10.1
    |            koulutusalaKoodiUrit:
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_054#1
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_055#1
    |            kuvaus:
    |              fi: Suomenkielinen kuvaus
    |              sv: Ruotsinkielinen kuvaus
    |        muokkaaja:
    |          type: string
    |          description: Valintaperustekuvausta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Valintaperustekuvauksen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        modified:
    |           type: string
    |           format: date-time
    |           description: Valintaperustekuvauksen viimeisin muokkausaika. Järjestelmän generoima
    |           example: 2019-08-23T09:55
    |"""
)
case class Valintaperuste(
    id: Option[UUID],
    externalId: Option[String],
    tila: Julkaisutila,
    esikatselu: Option[Boolean] = Some(true),
    koulutustyyppi: Koulutustyyppi,
    hakutapaKoodiUri: Option[String],
    kohdejoukkoKoodiUri: Option[String],
    nimi: Kielistetty,
    julkinen: Boolean,
    valintakokeet: List[Valintakoe],
    metadata: Option[ValintaperusteMetadata],
    organisaatioOid: OrganisaatioOid,
    muokkaaja: UserOid,
    kielivalinta: Seq[Kieli],
    modified: Option[Modified]
) extends PerustiedotWithId[Valintaperuste]
    with AuthorizableMaybeJulkinen[Valintaperuste] {
  override def withMuokkaaja(muokkaaja: UserOid): Valintaperuste = copy(muokkaaja = muokkaaja)
}
