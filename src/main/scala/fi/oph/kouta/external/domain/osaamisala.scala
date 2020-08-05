package fi.oph.kouta.external.domain

import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    Osaamisala:
    |      type: object
    |      properties:
    |        koodiUri:
    |          type: string
    |          description: Osaamisalan koodi URI. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/osaamisala/1)
    |          example: osaamisala_0001#1
    |        linkki:
    |          type: object
    |          description: Osaamisalan linkki
    |          allOf:
    |            - $ref: '#/components/schemas/Linkki'
    |        otsikko:
    |          type: object
    |          description: Osaamisalan linkin otsikko
    |          allOf:
    |            - $ref: '#/components/schemas/Teksti'
    |""")
sealed trait Osaamisala {
  val linkki: Kielistetty
  val otsikko: Kielistetty
}

case class AmmatillinenOsaamisala(koodiUri: String, linkki: Kielistetty, otsikko: Kielistetty) extends Osaamisala

@SwaggerModel(
  """    KorkeakouluOsaamisala:
    |      type: object
    |      properties:
    |        nimi:
    |          type: object
    |          description: Korkeakoulututkinnon erikoistumisalan, opintosuunnan, pääaineen tms. nimi
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        kuvaus:
    |          type: object
    |          description: Korkeakoulututkinnon erikoistumisalan, opintosuunnan, pääaineen tms. kuvaus
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        linkki:
    |          type: object
    |          description: Korkeakoulututkinnon erikoistumisalan, opintosuunnan, pääaineen tms. linkki
    |          allOf:
    |            - $ref: '#/components/schemas/Linkki'
    |        otsikko:
    |          type: object
    |          description: Korkeakoulututkinnon erikoistumisalan, opintosuunnan, pääaineen tms. linkin otsikko
    |          allOf:
    |            - $ref: '#/components/schemas/Teksti'
    |""")
case class KorkeakouluOsaamisala(nimi: Kielistetty, kuvaus: Kielistetty, linkki: Kielistetty, otsikko: Kielistetty)
  extends Osaamisala
