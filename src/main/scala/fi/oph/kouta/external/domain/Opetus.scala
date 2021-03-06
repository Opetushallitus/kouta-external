package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.Apurahayksikko
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
    |          $ref: '#/components/schemas/Kuvaus'
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
    |          $ref: '#/components/schemas/Kuvaus'
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
    |          $ref: '#/components/schemas/Kuvaus'
    |        onkoMaksullinen:
    |          type: boolean
    |          decription: "Onko koulutus maksullinen?"
    |        maksullisuusKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen maksullisuutta tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        koulutuksenAlkamiskausi:
    |          type: object
    |          description: Koulutuksen alkamiskausi
    |          $ref: '#/components/schemas/KoulutuksenAlkamiskausi'
    |        maksunMaara:
    |          type: double
    |          description: "Koulutuksen toteutuksen maksun määrä euroissa?"
    |          example: 220.50
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
    |          $ref: '#/components/schemas/Kuvaus'
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
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    maksunMaara: Option[Double],
    lisatiedot: Seq[Lisatieto],
    apuraha: Option[Apuraha],
    onkoApuraha: Option[Boolean],
)

@SwaggerModel(
"""    Apuraha:
  |      type: object
  |      properties:
  |        min:
  |          type: int
  |          description: Apurahan minimi euromäärä tai minimi prosenttiosuus lukuvuosimaksusta
  |          example: 100
  |        max:
  |          type: int
  |          description: Apurahan maksimi euromäärä tai maksimi prosenttiosuus lukuvuosimaksusta
  |          example: 200
  |        yksikko:
  |          type: string
  |          description: Apurahan yksikkö
  |          enum:
  |            - euro
  |            - prosentti
  |          example: euro
  |        kuvaus:
  |          type: object
  |          description: Koulutuksen toteutuksen apurahaa tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
  |          $ref: '#/components/schemas/Kuvaus'
  |""")
case class Apuraha(min: Option[Int],
                   max: Option[Int],
                   yksikko: Option[Apurahayksikko],
                   kuvaus: Kielistetty)