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
    |        kielitaitovaatimukset:
    |          type: array
    |          description: Lista valintaperustekuvauksen kielitaitovaatimuksista
    |          items:
    |            $ref: '#/components/schemas/Kielitaitovaatimus'
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/Valintatapa'
    |""")
sealed trait ValintaperusteMetadata {
  def tyyppi: Koulutustyyppi
  def valintatavat: Seq[Valintatapa]
  def kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimus]
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
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimus],
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
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimus],
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
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimus],
    osaamistaustaKoodiUrit: Seq[String],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadata

case class ValintaperusteKielitaitovaatimus(
    kieliKoodiUri: Option[String],
    kielitaidonVoiOsoittaa: Seq[Kielitaito],
    vaatimukset: Seq[Kielitaitovaatimus]
)

case class Kielitaito(kielitaitoKoodiUri: Option[String], lisatieto: Kielistetty = Map())

@SwaggerModel(
  """    Kielitaitovaatimus:
    |      type: object
    |      properties:
    |        kieliKoodiUri:
    |          type: string
    |          description: Kielitaitovaatimuksen kieli.
    |            Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          example: kieli_en#1
    |        kielitaidonVoiOsoittaa:
    |          type: array
    |          description: Lista tavoista, joilla kielitaidon voi osoittaa
    |          items:
    |            type: object
    |            properties:
    |              kielitaitoKoodiUri:
    |                type: string
    |                description: Kielitaidon osoittaminen.
    |                  Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kielitaidonosoittaminen/1)
    |                example: kielitaidonosoittaminen_01#1
    |              lisatieto:
    |                type: object
    |                description: Kielitaidon osoittamisen lisätieto eri kielillä.
    |                allOf:
    |                  - $ref: '#/components/schemas/Lisatieto'
    |        vaatimukset:
    |          type: array
    |          description: Lista kielitaitovaatimuksista
    |          items:
    |            type: object
    |            properties:
    |              kielitaitovaatimusKoodiUri:
    |                type: string
    |                description: Kielitaitovaatimuksen koodiUri.
    |                  Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kielitaitovaatimustyypit/1)
    |                example: kielitaitovaatimustyypit_01#1
    |              kielitaitovaatimusKuvaukset:
    |                type: array
    |                description: Lista kielitaitovaatimusten kuvauksia eri kielillä.
    |                items:
    |                  type: object
    |                  properties:
    |                    kielitaitovaatimusKuvausKoodiUri:
    |                      type: string
    |                      description: Kielitaitovaatimuksen kuvauksen koodiUri.
    |                        Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kielitaitovaatimustyypitkuvaus/1)
    |                      example: kielitaitovaatimustyypitkuvaus_01#1
    |                    kielitaitovaatimusTaso:
    |                      type: string
    |                      description: Kielitaitovaatimuksen taso
    |                      example: A
    |""")
case class Kielitaitovaatimus(
    kielitaitovaatimusKoodiUri: Option[String],
    kielitaitovaatimusKuvaukset: Seq[KielitaitovaatimusKuvaus]
)

case class KielitaitovaatimusKuvaus(
    kielitaitovaatimusKuvausKoodiUri: Option[String],
    kielitaitovaatimusTaso: Option[String]
)
