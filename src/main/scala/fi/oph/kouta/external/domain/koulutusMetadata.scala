package fi.oph.kouta.external.domain

import fi.oph.kouta.domain.{Amm, Erikoislaakari, Koulutustyyppi}
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    KoulutusMetadata:
    |      type: object
    |      properties:
    |        kuvaus:
    |          type: object
    |          description: Koulutuksen kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        lisatiedot:
    |          type: array
    |          description: Koulutukseen liittyviä lisätietoja, jotka näkyvät oppijalle Opintopolussa
    |          items:
    |            type: object
    |            $ref: '#/components/schemas/Lisatieto'
    |"""
)
sealed trait KoulutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val lisatiedot: Seq[Lisatieto]
}

@SwaggerModel("""    AmmatillinenKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm
    |              enum:
    |                - amm
    |""")
case class AmmatillinenKoulutusMetadata(
    tyyppi: Koulutustyyppi = Amm,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto]
) extends KoulutusMetadata

@SwaggerModel("""    AmmatillinenTutkinnonOsaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-tutkinnon-osa
    |              enum:
    |                - amm-tutkinnon-osa
    |            tutkinnonOsat:
    |              type: array
    |              description: Tutkinnon osat
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/TutkinnonOsa'
    |""")
case class AmmatillinenTutkinnonOsaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    tutkinnonOsat: Seq[TutkinnonOsa]
) extends KoulutusMetadata

@SwaggerModel(
  """    AmmatillinenOsaamisalaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-osaamisala
    |              enum:
    |                - amm-osaamisala
    |            osaamisalaKoodiUri:
    |              type: string
    |              description: Osaamisala. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/osaamisala/1)
    |              example: osaamisala_10#1
    |
    |"""
)
case class AmmatillinenOsaamisalaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    osaamisalaKoodiUri: Option[String]
) extends KoulutusMetadata

@SwaggerModel(
  """    KorkeakouluMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |      properties:
    |        koulutusalaKoodiUrit:
    |          type: array
    |          description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso2/1)
    |          items:
    |            type: string
    |            example:
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_054#1
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_055#1
    |        tutkintonimikeKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen tutkintonimikkeistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/tutkintonimikekk/2)
    |          items:
    |            type: string
    |          example:
    |            - tutkintonimikekk_110#2
    |            - tutkintonimikekk_111#2
    |        opintojenLaajuusyksikkoKoodiUri:
    |          type: string
    |          description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |          example: opintojenlaajuusyksikko_2#1
    |        opintojenLaajuusNumero:
    |          type: double
    |          description: Opintojen laajuus tai kesto numeroarvona
    |          example: 10
    |"""
)
trait KorkeakoulutusKoulutusMetadata extends KoulutusMetadata {
  val tutkintonimikeKoodiUrit: Seq[String]
  val opintojenLaajuusyksikkoKoodiUri: Option[String]
  val opintojenLaajuusNumero: Option[Double]
  val koulutusalaKoodiUrit: Seq[String]
}

@SwaggerModel("""    YliopistoKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: yo
    |              enum:
    |                - yo
    |""")
case class YliopistoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumero: Option[Double]
) extends KorkeakoulutusKoulutusMetadata

@SwaggerModel("""    AmmattikorkeaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amk
    |              enum:
    |                - amk
    |""")
case class AmmattikorkeakouluKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumero: Option[Double]
) extends KorkeakoulutusKoulutusMetadata

@SwaggerModel("""    AmmOpeErityisopeJaOpoKoulutusMetadata:
     |      allOf:
     |        - $ref: '#/components/schemas/KorkeakouluMetadata'
     |        - type: object
     |          properties:
     |            tyyppi:
     |              type: string
     |              description: Koulutuksen metatiedon tyyppi
     |              example: amm-ope-erityisope-ja-opo
     |              enum:
     |                - amm-ope-erityisope-ja-opo
     |""")
case class AmmOpeErityisopeJaOpoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumero: Option[Double]
) extends KorkeakoulutusKoulutusMetadata

@SwaggerModel("""    OpePedagOpinnotKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: ope-pedag-opinnot
    |              enum:
    |                - ope-pedag-opinnot
    |""")
case class OpePedagOpinnotKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumero: Option[Double]
) extends KorkeakoulutusKoulutusMetadata

@SwaggerModel(
  """    LukioKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: lk
    |              enum:
    |                - lk
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_2#1
    |            opintojenLaajuusNumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 150
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |"""
)
case class LukioKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumero: Option[Double],
    koulutusalaKoodiUrit: Seq[String]
) extends KoulutusMetadata

@SwaggerModel(
  """    TuvaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: tuva
    |              enum:
    |                - tuva
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_8#1
    |            opintojenLaajuusNumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 150
    |"""
)
case class TuvaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double]
) extends KoulutusMetadata

@SwaggerModel(
  """    TelmaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: telma
    |              enum:
    |                - telma
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusNumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 150
    |"""
)
case class TelmaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double]
) extends KoulutusMetadata

@SwaggerModel(
  """    AmmatillinenMuuKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-muu
    |              enum:
    |                - amm-muu
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusNumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 10
    |"""
)
case class AmmatillinenMuuKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String] = Seq(),
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    VapaaSivistystyoKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: vapaa-sivistystyo-opistovuosi
    |              enum:
    |                - vapaa-sivistystyo-opistovuosi
    |                - vapaa-sivistystyo-muu
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusNumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 10
    |"""
)
case class VapaaSivistystyoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    koulutusalaKoodiUrit: Seq[String] = Seq(),
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double]
) extends KoulutusMetadata

@SwaggerModel(
  """    AikuistenPerusopetusKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: aikuisten-perusopetus
    |              enum:
    |                - aikuisten-perusopetus
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusNumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 10
    |"""
)
case class AikuistenPerusopetusKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    KkOpintojaksoKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: kk-opintojakso
    |              enum:
    |                - kk-opintojakso
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_2#1
    |            opintojenLaajuusNumeroMin:
    |              type: integer
    |              description: Opintojen laajuuden tai keston vähimmäismäärä numeroarvona
    |              example: 10
    |            opintojenLaajuusNumeroMax:
    |              type: integer
    |              description: Opintojen laajuuden tai keston enimmäismäärä numeroarvona
    |              example: 20
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |"""
)
case class KkOpintojaksoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    koulutusalaKoodiUrit: Seq[String] = Seq(),
    isAvoinKorkeakoulutus: Option[Boolean],
    tunniste: Option[String] = None,
    opinnonTyyppiKoodiUri: Option[String] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    ErikoislaakariKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: erikoislaakari
    |              enum:
    |                - erikoislaakari
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |            tutkintonimikeKoodiUrit:
    |              type: array
    |              description: Lista koulutuksen tutkintonimikkeistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/tutkintonimikekk/2)
    |              items:
    |                type: string
    |              example:
    |                - tutkintonimikekk_110#2
    |                - tutkintonimikekk_111#2
    |"""
)
case class ErikoislaakariKoulutusMetadata(
    tyyppi: Koulutustyyppi = Erikoislaakari,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String] = Seq(),
    tutkintonimikeKoodiUrit: Seq[String] = Seq()
) extends KoulutusMetadata

@SwaggerModel(
  """    KkOpintokokonaisuusKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: kk-opintokokonaisuus
    |              enum:
    |                - kk-opintokokonaisuus
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusNumeroMin:
    |              type: integer
    |              description: Opintojen laajuuden tai keston vähimmäismäärä numeroarvona
    |              example: 10
    |            opintojenLaajuusNumeroMax:
    |              type: integer
    |              description: Opintojen laajuuden tai keston enimmäismäärä numeroarvona
    |              example: 20
    |"""
)
case class KkOpintokokonaisuusKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    isAvoinKorkeakoulutus: Option[Boolean] = None,
    tunniste: Option[String] = None,
    opinnonTyyppiKoodiUri: Option[String] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    ErikoistumiskoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: erikoistumiskoulutus
    |              enum:
    |                - erikoistumiskoulutus
    |            erikoistumiskoulutusKoodiUri:
    |              type: string
    |              description: Erikoistumiskoulutuksen koodiURI. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/erikoistumiskoulutukset/2)
    |              example:
    |                - erikoistumiskoulutukset_001#2
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: Opintojen laajuusyksikko. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)
    |              example:
    |                - opintojenlaajuusyksikko_2#1
    |            opintojenLaajuusNumeroMin:
    |              type: integer
    |              description: Opintojen laajuuden tai keston vähimmäismäärä numeroarvona
    |              example: 10
    |            opintojenLaajuusNumeroMax:
    |              type: integer
    |              description: Opintojen laajuuden tai keston enimmäismäärä numeroarvona
    |              example: 20
    |"""
)
case class ErikoistumiskoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    erikoistumiskoulutusKoodiUri: Option[String] = None,
    koulutusalaKoodiUrit: Seq[String],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double]
) extends KoulutusMetadata

@SwaggerModel(
  """    TaiteenPerusopetusKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: taiteen-perusopetus
    |              enum:
    |                - taiteen-perusopetus
    |            linkkiEPerusteisiin:
    |              type: string
    |              description: Linkki koulutuksen eperusteisiin
    |              example: https://eperusteet.opintopolku.fi/#/fi/kooste/taiteenperusopetus
    |"""
)
case class TaiteenPerusopetusKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty
) extends KoulutusMetadata
