package fi.oph.kouta.external.domain

import fi.oph.kouta.domain._
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    ToteutusMetadata:
    |      type: object
    |      properties:
    |        kuvaus:
    |          type: object
    |          description: Toteutuksen kuvausteksti eri kielillä. Kielet on määritetty toteutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        opetus:
    |          type: object
    |          $ref: '#/components/schemas/Opetus'
    |        yhteyshenkilot:
    |          type: array
    |          description: Lista toteutuksen yhteyshenkilöistä
    |          items:
    |            $ref: '#/components/schemas/Yhteyshenkilo'
    |        asiasanat:
    |          type: array
    |          description: Lista toteutukseen liittyvistä asiasanoista, joiden avulla opiskelija voi hakea koulutusta Opintopolusta
    |          items:
    |            $ref: '#/components/schemas/Asiasana'
    |        ammattinimikkeet:
    |          type: array
    |          description: Lista toteutukseen liittyvistä ammattinimikkeistä, joiden avulla opiskelija voi hakea koulutusta Opintopolusta
    |          items:
    |            $ref: '#/components/schemas/Ammattinimike'
    |"""
)
sealed trait ToteutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val opetus: Option[Opetus]
  val asiasanat: List[Keyword]
  val ammattinimikkeet: List[Keyword]
  val yhteyshenkilot: Seq[Yhteyshenkilo]
}

@SwaggerModel(
  """    KorkeakouluToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |      properties:
    |        alemmanKorkeakoulututkinnonOsaamisalat:
    |          type: array
    |          description: Lista alemman korkeakoulututkinnon erikoistumisalojen, opintosuuntien, pääaineiden tms. kuvauksista.
    |          items:
    |            $ref: '#/components/schemas/KorkeakouluOsaamisala'
    |        ylemmanKorkeakoulututkinnonOsaamisalat:
    |          type: array
    |          items:
    |            $ref: '#/components/schemas/KorkeakouluOsaamisala'
    |          description: Lista ylemmän korkeakoulututkinnon erikoistumisalojen, opintosuuntien, pääaineiden tms. kuvauksista.
    |"""
)
sealed trait KorkeakoulutusToteutusMetadata extends ToteutusMetadata {
  val alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
  val ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
}

@SwaggerModel("""    AmmatillinenToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            osaamisalat:
    |              type: array
    |              items:
    |                $ref: '#/components/schemas/Osaamisala'
    |              description: Lista ammatillisen koulutuksen osaamisalojen kuvauksia
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm
    |              enum:
    |                - amm
    |            ammatillinenPerustutkintoErityisopetuksena:
    |              type: boolean
    |              description: Onko koulutuksen tyyppi \"Ammatillinen perustutkinto erityisopetuksena\"?
    |""")
case class AmmatillinenToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisala],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    ammatillinenPerustutkintoErityisopetuksena: Boolean
) extends ToteutusMetadata

@SwaggerModel(
  """    TutkintoonJohtamatonToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            hakutermi:
    |              type: object
    |              $ref: '#/components/schemas/Hakutermi'
    |            hakulomaketyyppi:
    |              type: string
    |              description: Hakulomakkeen tyyppi. Kertoo, käytetäänkö Atarun (hakemuspalvelun) hakulomaketta, muuta hakulomaketta
    |                (jolloin voidaan lisätä hakulomakkeeseen linkki) tai onko niin, ettei sähkököistä hakulomaketta ole lainkaan, jolloin sille olisi hyvä lisätä kuvaus.
    |              example: "ataru"
    |              enum:
    |                - ataru
    |                - haku-app
    |                - ei sähköistä
    |                - muu
    |            hakulomakeLinkki:
    |              type: object
    |              description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |              $ref: '#/components/schemas/Linkki'
    |            lisatietoaHakeutumisesta:
    |              type: object
    |              description: Lisätietoa hakeutumisesta eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |              $ref: '#/components/schemas/Teksti'
    |            lisatietoaValintaperusteista:
    |              type: object
    |              description: Lisätietoa valintaperusteista eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |              $ref: '#/components/schemas/Teksti'
    |            hakuaika:
    |              type: array
    |              description: Toteutuksen hakuaika
    |              $ref: '#/components/schemas/Ajanjakso'
    |            aloituspaikat:
    |              type: integer
    |              description: Toteutuksen aloituspaikkojen lukumäärä
    |              example: 100
    |"""
)
sealed trait TutkintoonJohtamatonToteutusMetadata extends ToteutusMetadata {
  def hakutermi: Option[Hakutermi]
  def hakulomaketyyppi: Option[Hakulomaketyyppi]
  def hakulomakeLinkki: Kielistetty
  def lisatietoaHakeutumisesta: Kielistetty
  def lisatietoaValintaperusteista: Kielistetty
  def hakuaika: Option[Ajanjakso]
  def aloituspaikat: Option[Int]
}

@SwaggerModel("""    AmmatillinenTutkinnonOsaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: amm-tutkinnon-osa
    |              enum:
    |                - amm-tutkinnon-osa
    |""")
case class AmmatillinenTutkinnonOsaToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel(
  """    AmmatillinenOsaamisalaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: amm-osaamisala
    |              enum:
    |                - amm-osaamisala
    |"""
)
case class AmmatillinenOsaamisalaToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    YliopistoToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: yo
    |              enum:
    |                - yo
    |""")
case class YliopistoToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
) extends KorkeakoulutusToteutusMetadata

@SwaggerModel("""    AmmattikorkeaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amk
    |              enum:
    |                - amk
    |""")
case class AmmattikorkeakouluToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
) extends KorkeakoulutusToteutusMetadata

@SwaggerModel(
  """    LukiolinjaTieto:
    |      type: object
    |      description: Toteutuksen yksittäisen lukiolinjatiedon kentät
    |      properties:
    |        koodiUri:
    |          type: string
    |          description: Lukiolinjatiedon koodiUri.
    |        kuvaus:
    |          type: object
    |          description: Lukiolinjatiedon kuvaus eri kielillä. Kielet on määritetty toteutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |"""
)
case class LukiolinjaTieto(koodiUri: String, kuvaus: Kielistetty)

@SwaggerModel(
  """    LukiodiplomiTieto:
    |      type: object
    |      description: Toteutuksen yksittäisen lukiodiplomitiedon kentät
    |      properties:
    |        koodiUri:
    |          type: string
    |          description: Lukiodiplomin koodiUri. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/moduulikoodistolops2021/1).
    |        linkki:
    |          type: object
    |          description: Lukiodiplomin kielistetyt lisätietolinkit. Kielet on määritetty toteutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Linkki'
    |        linkinAltTeksti:
    |          type: object
    |          description: Lukiodiplomin kielistettyjen lisätietolinkkien alt-tekstit. Kielet on määritetty toteutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Teksti'
    |"""
)
case class LukiodiplomiTieto(koodiUri: String, linkki: Kielistetty, linkinAltTeksti: Kielistetty)

@SwaggerModel(
  """    LukioToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: lk
    |              enum:
    |                - lk
    |            kielivalikoima:
    |              type: object
    |              description: Koulutuksen kielivalikoima
    |              $ref: '#/components/schemas/Kielivalikoima'
    |            yleislinja:
    |              type: boolean,
    |              description: Onko lukio-toteutuksella yleislinja?
    |            painotukset:
    |              type: array
    |              description: Lukio-toteutuksen painotukset. Taulukon alkioiden koodiUri-kentät viittaavat [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/lukiopainotukset/1).
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/LukiolinjaTieto'
    |            erityisetKoulutustehtavat:
    |              type: array
    |              description: Lukio-toteutuksen erityiset koulutustehtävät. Taulukon alkioiden koodiUri-kentät viittaavat [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/lukiolinjaterityinenkoulutustehtava/1).
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/LukiolinjaTieto'
    |            diplomit:
    |              type: array
    |              description: Lukio-toteutuksen diplomit
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/LukiodiplomiTieto'
    |"""
)
case class LukioToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    kielivalikoima: Option[Kielivalikoima],
    yleislinja: Boolean,
    painotukset: Seq[LukiolinjaTieto],
    erityisetKoulutustehtavat: Seq[LukiolinjaTieto],
    diplomit: Seq[LukiodiplomiTieto]
) extends ToteutusMetadata

@SwaggerModel("""    TuvaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: tuva
    |              enum:
    |                - tuva
    |            tuvaErityisopetuksena:
    |              type: boolean
    |              description: Tieto siitä järjestetäänkö toteutus erityisopetuksena
    |""")
case class TuvaToteutusMetadata(
    tyyppi: Koulutustyyppi = Tuva,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    tuvaErityisopetuksena: Boolean
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    TelmaToteutusMetadata:
                |      allOf:
                |        - $ref: '#/components/schemas/ToteutusMetadata'
                |        - type: object
                |          properties:
                |            koulutustyyppi:
                |              type: string
                |              description: Koulutuksen metatiedon tyyppi
                |              example: telma
                |              enum:
                |                - telma
                |            tuvaErityisopetuksena:
                |              type: boolean
                |              description: Tieto siitä järjestetäänkö toteutus erityisopetuksena
                |""")
case class TelmaToteutusMetadata(
    tyyppi: Koulutustyyppi = Telma,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    aloituspaikat: Option[Int]
) extends ToteutusMetadata

@SwaggerModel("""    VapaaSivistystyoOpistovuosiToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: vapaa-sivistystyo-opistovuosi
    |              enum:
    |                - vapaa-sivistystyo-opistovuosi
    |""")
case class VapaaSivistystyoOpistovuosiToteutusMetadata(
    tyyppi: Koulutustyyppi = VapaaSivistystyoOpistovuosi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadata

@SwaggerModel("""    VapaaSivistystyoMuuToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: vapaa-sivistystyo-muu
    |              enum:
    |                - vapaa-sivistystyo-muu
    |""")
case class VapaaSivistystyoMuuToteutusMetadata(
    tyyppi: Koulutustyyppi = VapaaSivistystyoMuu,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata
