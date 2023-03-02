package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.{Apurahayksikko, Maksullisuustyyppi}
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    Opetus:
    |      type: object
    |      properties:
    |        opetuskieliKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen toteutuksen opetuskielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/oppilaitoksenopetuskieli/1)
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
    |          description: Lista koulutuksen toteutuksen opetusajoista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/opetusaikakk/1)
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
    |          description: Lista koulutuksen toteutuksen opetustavoista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/opetuspaikkakk/1)
    |          items:
    |            type: string
    |            example:
    |              - opetuspaikkakk_2#1
    |              - opetuspaikkakk_2#1
    |        opetustapaKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen opetustapoja tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        maksullisuustyyppi:
    |          type: string
    |          description: Maksullisuuden tyyppi
    |          enum:
    |            - 'maksullinen'
    |            - 'maksuton'
    |            - 'lukuvuosimaksu'
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
    |        onkoApuraha:
    |          type: boolean
    |          description: Onko koulutukseen apurahaa?
    |        apuraha:
    |          type: object
    |          description: Koulutuksen apurahatiedot
    |          $ref: '#/components/schemas/Apuraha'
    |        suunniteltuKestoVuodet:
    |          type: integer
    |          description: "Koulutuksen suunniteltu kesto vuosina"
    |          example: 2
    |        suunniteltuKestoKuukaudet:
    |          type: integer
    |          description: "Koulutuksen suunniteltu kesto kuukausina"
    |          example: 2
    |        suunniteltuKestoKuvaus:
    |          type: object
    |          description: "Koulutuksen toteutuksen suunnitellun keston kuvaus eri kielillä. Kielet on määritetty toteutuksen kielivalinnassa."
    |          $ref: '#/components/schemas/Kuvaus'
    |""")
case class Opetus(
    opetuskieliKoodiUrit: Seq[String],
    opetuskieletKuvaus: Kielistetty,
    opetusaikaKoodiUrit: Seq[String],
    opetusaikaKuvaus: Kielistetty = Map(),
    opetustapaKoodiUrit: Seq[String],
    opetustapaKuvaus: Kielistetty,
    maksullisuustyyppi: Option[Maksullisuustyyppi],
    maksullisuusKuvaus: Kielistetty = Map(),
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    maksunMaara: Option[Double],
    lisatiedot: Seq[Lisatieto],
    onkoApuraha: Boolean,
    apuraha: Option[Apuraha],
    suunniteltuKestoVuodet: Option[Int],
    suunniteltuKestoKuukaudet: Option[Int],
    suunniteltuKestoKuvaus: Kielistetty
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