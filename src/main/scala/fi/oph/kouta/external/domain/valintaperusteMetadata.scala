package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.Koulutustyyppi
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    ValintaperusteMetadata:
    |      type: object
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: "Koulutuksen tyyppi. Sallitut arvot: 'amm' (ammatillinen), 'yo' (yliopisto), 'lk' (lukio), 'amk' (ammattikorkea), 'amm-tutkinnon-osa', 'amm-osaamisala'"
    |          enum:
    |            - amm
    |            - yo
    |            - amk
    |            - lk
    |            - amm-tutkinnon-osa
    |            - amm-osaamisala
    |          example: amm
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/Valintatapa'
    |        valintakokeidenYleiskuvaus:
    |          type: object
    |          description: Valintakokeiden yleiskuvaus eri kielillä. Kielet on määritetty valintaperustekuvauksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        kuvaus:
    |          type: object
    |          description: Valintaperustekuvauksen kuvausteksti eri kielillä. Kielet on määritetty valintaperustekuvauksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |""")
sealed trait ValintaperusteMetadata {
  def tyyppi: Koulutustyyppi
  def valintatavat: Seq[Valintatapa]
  def valintakokeidenYleiskuvaus: Kielistetty
  def kuvaus: Kielistetty
}

@SwaggerModel(
  """    KorkeakoulutusValintaperusteMetadata:
    |      type: object
    |      properties:
    |        osaamistaustaKoodiUrit:
    |          type: array
    |          description: Lista valintaperustekuvauksen osaamistaustoista.
    |            Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/osaamistausta/1)
    |          items:
    |            - type: string
    |          example:
    |            - osaamistausta_001#1
    |            - osaamistausta_002#1
    |        kuvaus:
    |          type: object
    |          description: Valintaperustekuvauksen kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |""")
sealed trait KorkeakoulutusValintaperusteMetadata extends ValintaperusteMetadata {
  def osaamistaustaKoodiUrit: Seq[String]
}

@SwaggerModel(
  """    AmmatillinenValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amm
    |          enum:
    |            - amm
    |""")
case class AmmatillinenValintaperusteMetadata(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[Valintatapa],
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
    kuvaus: Kielistetty
) extends ValintaperusteMetadata

@SwaggerModel(
  """    YliopistoValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakoulutusValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: yo
    |          enum:
    |            - yo
    |""")
case class YliopistoValintaperusteMetadata(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[Valintatapa],
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
    osaamistaustaKoodiUrit: Seq[String],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadata

@SwaggerModel(
  """    AmmattikorkeakouluValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakoulutusValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amk
    |          enum:
    |            - amk
    |""")
case class AmmattikorkeakouluValintaperusteMetadata(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[Valintatapa],
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
    osaamistaustaKoodiUrit: Seq[String],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadata