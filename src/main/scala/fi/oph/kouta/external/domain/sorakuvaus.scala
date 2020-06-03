package fi.oph.kouta.external.domain

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.domain.oid.{OrganisaatioOid, UserOid}
import fi.oph.kouta.domain.{Julkaisutila, Kieli, Koulutustyyppi}
import fi.oph.kouta.swagger.SwaggerModel
import fi.oph.kouta.security.AuthorizableMaybeJulkinen

@SwaggerModel(
  """    Sorakuvaus:
    |      type: object
    |      properties:
    |        id:
    |          type: string
    |          description: SORA-kuvauksen yksilöivä tunniste. Järjestelmän generoima.
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        koulutustyyppi:
    |          type: string
    |          description: "Minkä tyyppisiin koulutuksiin SORA-kuvaus liittyy. Sallitut arvot: 'amm' (ammatillinen), 'yo' (yliopisto), 'lk' (lukio), 'amk' (ammattikorkea), 'muu' (muu koulutus)"
    |          enum:
    |            - amm
    |            - yo
    |            - amk
    |            - lk
    |            - muu
    |          example: amm
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |          description: SORA-kuvauksen julkaisutila. Jos SORA-kuvaus on julkaistu, se näkyy oppijalle Opintopolussa.
    |        julkinen:
    |          type: boolean
    |          description: Voivatko muut oppilaitokset käyttää SORA-kuvausta
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille SORA-kuvauksen nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        nimi:
    |          type: object
    |          description: SORA-kuvauksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty SORA-kuvauksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        metadata:
    |          type: object
    |          description: SORA-kuvauksen kuvailutiedot eri kielillä
    |          properties:
    |            kuvaus:
    |              type: object
    |              description: SORA-kuvauksen kuvausteksti eri kielillä. Kielet on määritetty kuvauksen kielivalinnassa.
    |              allOf:
    |                - $ref: '#/components/schemas/Kuvaus'
    |        muokkaaja:
    |          type: string
    |          description: SORA-kuvausta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: SORA-kuvauksen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        modified:
    |           type: string
    |           format: date-time
    |           description: SORA-kuvauksen viimeisin muokkausaika. Järjestelmän generoima
    |           example: 2019-08-23T09:55
    |""")
case class Sorakuvaus(
    id: Option[UUID],
    tila: Julkaisutila,
    nimi: Kielistetty,
    koulutustyyppi: Koulutustyyppi,
    julkinen: Boolean,
    kielivalinta: Seq[Kieli],
    metadata: Option[SorakuvausMetadata],
    organisaatioOid: OrganisaatioOid,
    muokkaaja: UserOid,
    modified: Option[LocalDateTime]
) extends PerustiedotWithId[Sorakuvaus] with AuthorizableMaybeJulkinen[Sorakuvaus] {
  override def withMuokkaaja(muokkaaja: UserOid): Sorakuvaus = copy(muokkaaja = muokkaaja)
}

case class SorakuvausMetadata(kuvaus: Kielistetty = Map())
