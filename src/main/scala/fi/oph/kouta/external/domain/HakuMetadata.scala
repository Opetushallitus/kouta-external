package fi.oph.kouta.external.domain

import fi.oph.kouta.external.swagger.SwaggerModel

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
    |""")
case class HakuMetadata(
    yhteyshenkilot: Seq[Yhteyshenkilo],
    tulevaisuudenAikataulu: Seq[Ajanjakso],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi]
)
