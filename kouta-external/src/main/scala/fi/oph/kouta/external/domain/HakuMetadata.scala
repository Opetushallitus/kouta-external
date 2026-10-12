package fi.oph.kouta.external.domain

import fi.oph.kouta.external.swagger.SwaggerModel

import java.time.LocalDateTime

@SwaggerModel(
  """    HakuMetadata:
    |      type: object
    |      properties:
    |        yhteyshenkilot:
    |          type: array
    |          description: Haun yhteyshenkilöiden tiedot
    |          items:
    |            $ref: '#/components/schemas/Yhteyshenkilo'
    |        tulevaisuudenAikataulu:
    |          type: array
    |          description: Oppijalle Opintopolussa näytettävät haun mahdolliset tulevat hakuajat
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
    |        koulutuksenAlkamiskausi:
    |          type: object
    |          description: Koulutuksen alkamiskausi
    |          $ref: '#/components/schemas/KoulutuksenAlkamiskausi'
    |        varasijatayttoPaattyy:
    |          type: string
    |          format: date-time
    |          description: Haun varasijatäytön päättymisen ajankohta. Käytetään oma-opiskelijavalinnassa hakijan tiedottamiseen.
    |          example: 2019-08-23T09:55
    |"""
)
case class HakuMetadata(
    yhteyshenkilot: Seq[Yhteyshenkilo],
    tulevaisuudenAikataulu: Seq[Ajanjakso],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    varasijatayttoPaattyy: Option[LocalDateTime]
)
