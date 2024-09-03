package fi.oph.kouta.external.domain

import fi.oph.kouta.domain._
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    ValintaperusteMetadata:
    |      type: object
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon (koulutus)tyyppi
    |          $ref: '#/components/schemas/Koulutustyyppi'
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

case class GenericValintaperusteMetadata(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[Valintatapa],
    kuvaus: Kielistetty,
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto] = Seq(),
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata
