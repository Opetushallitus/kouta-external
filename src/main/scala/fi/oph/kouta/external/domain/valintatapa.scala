package fi.oph.kouta.external.domain

import java.util.UUID

import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    Valintatapa:
    |      type: object
    |      properties:
    |        valintatapaKoodiUri:
    |          type: string
    |          description: Valintatapa. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/valintatapajono/1)
    |          example: valintatapajono_av#1
    |        nimi:
    |          type: object
    |          description: Valintatapakuvauksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        sisalto:
    |          type: array
    |          description: Valintatavan sisältö. Voi sisältää sekä teksti- että taulukkoelementtejä.
    |          items:
    |            type: object
    |            oneOf:
    |              - $ref: '#/components/schemas/SisaltoTeksti'
    |              - $ref: '#/components/schemas/SisaltoTaulukko'
    |        kaytaMuuntotaulukkoa:
    |          type: boolean
    |          description: "Käytetäänkö muuntotaulukkoa?"
    |        kynnysehto:
    |          type: object
    |          description: Kynnysehdon kuvausteksti eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        enimmaispisteet:
    |          type: double
    |          description: Valintatavan enimmäispisteet
    |          example: 20.0
    |        vahimmaispisteet:
    |          type: double
    |          description: Valintatavan vähimmäispisteet
    |          example: 10.0
    |""")
case class Valintatapa(
    nimi: Kielistetty,
    valintatapaKoodiUri: Option[String],
    sisalto: Seq[Sisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
)

sealed trait Sisalto

@SwaggerModel(
  """    SisaltoTaulukko:
    |      type: object
    |      description: Taulukkomuotoinen valintatavan sisällön kuvaus
    |      properties:
    |        id:
    |          type: string
    |          description: Taulukon yksilöivä tunnus
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        nimi:
    |          type: object
    |          description: Taulukon Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        rows:
    |          type: array
    |          description: Taukon rivit
    |          items:
    |            type: object
    |            properties:
    |              index:
    |                type: integer
    |                description: Rivin järjestysnumero
    |              isHeader:
    |                type: boolean
    |                description: Onko rivi otsikkorivi
    |              columns:
    |                type: array
    |                description: Rivin sarakkeet
    |                items:
    |                  type: object
    |                  properties:
    |                    index:
    |                      type: integer
    |                      description: Sarakkeen järjestysnumero
    |                    text:
    |                      type: object
    |                      description: Sarakkeen Opintopolussa näytettävä teksti eri kielillä.
    |                        Kielet on määritetty valintaperusteen kielivalinnassa.
    |                      allOf:
    |                        - $ref: '#/components/schemas/Teksti'
    |""")
case class Taulukko(id: Option[UUID], nimi: Kielistetty, rows: Seq[Row]) extends Sisalto

@SwaggerModel(
  """    SisaltoTeksti:
    |      type: object
    |      description: Tekstimuotoinen valintatavan sisällön kuvaus
    |      properties:
    |        teksti:
    |          type: object
    |          description: Valintatavan Opintopolussa näytettävä kuvausteksti eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Teksti'
    |""")
case class SisaltoTeksti(teksti: Kielistetty) extends Sisalto

case class Row(index: Int, isHeader: Boolean, columns: Seq[Column])

case class Column(index: Int, text: Kielistetty)
