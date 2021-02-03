package fi.oph.kouta.external.domain

import java.time.LocalDateTime

import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    Opetus:
    |      type: object
    |      properties:
    |        opetuskieliKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen toteutuksen opetuskielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/oppilaitoksenopetuskieli/1)
    |          items:
    |            type: string
    |            example:
    |              - oppilaitoksenopetuskieli_1#1
    |              - oppilaitoksenopetuskieli_2#1
    |        opetuskieletKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen opetuskieliä tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        opetusaikaKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen toteutuksen opetusajoista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opetusaikakk/1)
    |          items:
    |            type: string
    |            example:
    |              - opetusaikakk_1#1
    |              - opetusaikakk_2#1
    |        opetusaikaKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen opetusaikoja tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        opetustapaKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen toteutuksen opetustavoista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opetuspaikkakk/1)
    |          items:
    |            type: string
    |            example:
    |              - opetuspaikkakk_2#1
    |              - opetuspaikkakk_2#1
    |        opetustapaKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen opetustapoja tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        onkoMaksullinen:
    |          type: boolean
    |          decription: "Onko koulutus maksullinen?"
    |        maksullisuusKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen maksullisuutta tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        maksunMaara:
    |          type: double
    |          description: "Koulutuksen toteutuksen maksun määrä euroissa?"
    |          example: 220.50
    |        koulutuksenTarkkaAlkamisaika:
    |          type: string
    |          description: Jos alkamisaika on tiedossa niin alkamis- ja päättymispäivämäärä on pakollinen. Muussa tapauksessa kausi ja vuosi on pakollisia tietoja.
    |          example: true
    |        koulutuksenAlkamispaivamaara:
    |          type: string
    |          description: Koulutuksen alkamisen päivämäärä
    |          example: 2019-11-20T12:00
    |        koulutuksenPaattymispaivamaara:
    |          type: string
    |          description: Koulutuksen päättymisen päivämäärä
    |          example: 2019-12-20T12:00
    |        koulutuksenAlkamiskausi:
    |          type: string
    |          description: Koulutuksen toteutuksen alkamiskausi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kausi/1)
    |          example: kausi_k#1
    |        koulutuksenAlkamisvuosi:
    |          type: string
    |          description: Koulutuksen alkamisvuosi
    |          example: 2020
    |        lisatiedot:
    |          type: array
    |          description: Koulutuksen toteutukseen liittyviä lisätietoja, jotka näkyvät oppijalle Opintopolussa
    |          items:
    |            type: object
    |            $ref: '#/components/schemas/Lisatieto'
    |        onkoStipendia:
    |          type: boolean
    |          description: "Onko koulutukseen stipendiä?"
    |        stipendinMaara:
    |          type: double
    |          description: Koulutuksen toteutuksen stipendin määrä.
    |          example: 10.0
    |        stipendinKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen stipendiä tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |""")
case class Opetus(
    opetuskieliKoodiUrit: Seq[String],
    opetuskieletKuvaus: Kielistetty,
    opetusaikaKoodiUrit: Seq[String],
    opetusaikaKuvaus: Kielistetty,
    opetustapaKoodiUrit: Seq[String],
    opetustapaKuvaus: Kielistetty,
    onkoMaksullinen: Option[Boolean],
    maksullisuusKuvaus: Kielistetty,
    maksunMaara: Option[Double],
    lisatiedot: Seq[Lisatieto],
    onkoStipendia: Option[Boolean],
    stipendinMaara: Option[Double],
    stipendinKuvaus: Kielistetty
)
