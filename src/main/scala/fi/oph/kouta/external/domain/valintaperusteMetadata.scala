package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.{Amk, Amm, AmmOsaamisala, AmmTutkinnonOsa, Koulutustyyppi, Lk, Muu, Tuva, Yo}
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
    |""")
sealed trait ValintaperusteMetadata {
  def tyyppi: Koulutustyyppi
  def valintatavat: Seq[Valintatapa]
  def kuvaus: Kielistetty
  def hakukelpoisuus: Kielistetty
  def lisatiedot: Kielistetty
  def valintakokeidenYleiskuvaus: Kielistetty
  def sisalto: Seq[Sisalto]
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
    tyyppi: Koulutustyyppi = Amm,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty,
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel(
"""    LukioValintaperusteMetadata:
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

@SwaggerModel(
  """    YliopistoValintaperusteMetadata:
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
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
) extends ValintaperusteMetadata

@SwaggerModel(
  """    AmmattikorkeakouluValintaperusteMetadata:
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
    valintakokeidenYleiskuvaus: Kielistetty = Map(),
) extends ValintaperusteMetadata


@SwaggerModel(
  """    AmmatillinenTutkinnonOsaValintaperusteMetadata:
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

@SwaggerModel(
  """    AmmatillinenOsaamisalaValintaperusteMetadata:
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

@SwaggerModel(
  """    TutkintokoulutukseenValmentavaValintaperusteMetadata:
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
case class TutkintokoulutukseenValmentavaValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Tuva,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel(
  """    MuuValintaperusteMetadata:
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