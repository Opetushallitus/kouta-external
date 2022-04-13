package fi.oph.kouta.external.domain

import fi.oph.kouta.domain._
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    ValintaperusteMetadata:
    |      type: object
    |      properties:
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/Valintatapa'
    |        kuvaus:
    |          type: object
    |          description: Valintaperustekuvauksen kuvausteksti eri kielillä. Kielet on määritetty valintaperustekuvauksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        hakukelpoisuus:
    |          type: object
    |          description: Valintaperustekuvauksen hakukelpoisuus eri kielillä. Kielet on määritetty valintaperustekuvauksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        lisatiedot:
    |          type: object
    |          description: Valintaperustekuvauksen lisatiedot eri kielillä. Kielet on määritetty valintaperustekuvauksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        valintakokeidenYleiskuvaus:
    |          type: object
    |          description: Valintakokeiden yleiskuvaus eri kielillä. Kielet on määritetty valintaperustekuvauksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        sisalto:
    |          type: array
    |          description: Valintaperusteen kuvauksen sisältö. Voi sisältää sekä teksti- että taulukkoelementtejä.
    |          items:
    |            type: object
    |            oneOf:
    |              - $ref: '#/components/schemas/SisaltoTeksti'
    |              - $ref: '#/components/schemas/SisaltoTaulukko'
    |"""
)
sealed trait ValintaperusteMetadata {
  def tyyppi: Koulutustyyppi
  def valintatavat: Seq[Valintatapa]
  def kuvaus: Kielistetty
  def hakukelpoisuus: Kielistetty
  def lisatiedot: Kielistetty
  def valintakokeidenYleiskuvaus: Kielistetty
  def sisalto: Seq[Sisalto]
}

@SwaggerModel("""    AmmatillinenValintaperusteMetadata:
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
    tyyppi: Koulutustyyppi = Amm,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty,
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    LukioValintaperusteMetadata:
  |      type: object
  |      allOf:
  |        - $ref: '#/components/schemas/ValintaperusteMetadata'
  |      properties:
  |        tyyppi:
  |          type: string
  |          description: Valintaperustekuvauksen metatiedon tyyppi
  |          example: lk
  |          enum:
  |            - lk
  |""")
case class LukioValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Lk,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    YliopistoValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: yo
    |          enum:
    |            - yo
    |""")
case class YliopistoValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Yo,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty,
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    AmmattikorkeakouluValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amk
    |          enum:
    |            - amk
    |""")
case class AmmattikorkeakouluValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Amk,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty,
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    AmmOpeErityisopeJaOpoValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/Valintatapa'
    |        koulutustyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amm-ope-erityisope-ja-opo
    |          enum:
    |            - amm-ope-erityisope-ja-opo
    |""")
case class AmmOpeErityisopeJaOpoValintaperusteMetadata(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty,
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    AmmatillinenTutkinnonOsaValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amm-tutkinnon-osa
    |          enum:
    |            - amm-tutkinnon-osa
    |""")
case class AmmatillinenTutkinnonOsaValintaperusteMetadata(
    tyyppi: Koulutustyyppi = AmmTutkinnonOsa,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    AmmatillinenOsaamisalaValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amm-osaamisala
    |          enum:
    |            - amm-osaamisala
    |""")
case class AmmatillinenOsaamisalaValintaperusteMetadata(
    tyyppi: Koulutustyyppi = AmmOsaamisala,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    TuvaValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: tuva
    |          enum:
    |            - tuva
    |""")
case class TuvaValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Tuva,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    TelmaValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/ValintaperusteMetadata'
                |      properties:
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: telma
                |          enum:
                |            - telma
                |""")
case class TelmaValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Telma,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    VapaaSivistystyoValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: vapaa-sivistystyo-opistovuosi
    |          enum:
    |            - vapaa-sivistystyo-opistovuosi
    |            - vapaa-sivistystyo-muu
    |""")
case class VapaaSivistystyoValintaperusteMetadata(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    MuuValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: muu
    |          enum:
    |            - muu
    |""")
case class MuuValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Muu,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata
