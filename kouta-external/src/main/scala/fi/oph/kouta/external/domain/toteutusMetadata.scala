package fi.oph.kouta.external.domain

import fi.oph.kouta.domain._
import fi.oph.kouta.domain.oid.{KoulutusOid, ToteutusOid}
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
    |        hasJotpaRahoitus:
    |          type: boolean
    |          description: Onko toteutus JOTPA-rahoitettua?
    |        isTyovoimakoulutus:
    |          type: boolean
    |          description: Onko toteutus työvoimakoulutusta?
    |        isTaydennyskoulutus:
    |          type: boolean
    |          description: Onko toteutus täydennyskoulutusta?
    |"""
)
sealed trait ToteutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val opetus: Option[Opetus]
  val asiasanat: List[Keyword]
  val ammattinimikkeet: List[Keyword]
  val yhteyshenkilot: Seq[Yhteyshenkilo]
  val hasJotpaRahoitus: Option[Boolean]
  val isTaydennyskoulutus: Boolean
  val isTyovoimakoulutus: Boolean
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
    |            tyyppi:
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
    tyyppi: Koulutustyyppi = Amm,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisala],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    ammatillinenPerustutkintoErityisopetuksena: Boolean,
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel(
  """    TutkintoonJohtamatonToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            isHakukohteetKaytossa:
    |              type: boolean
    |              description: Tieto siitä onko toteutuksella käytössä hakukohteet
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
    |            aloituspaikkakuvaus:
    |              type: object
    |              description: Koulutuksen toteutuksen aloituspaikkoja tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |              $ref: '#/components/schemas/Teksti'
    |
    |"""
)
sealed trait TutkintoonJohtamatonToteutusMetadata extends ToteutusMetadata {
  def isHakukohteetKaytossa: Option[Boolean]
  def hakutermi: Option[Hakutermi]
  def hakulomaketyyppi: Option[Hakulomaketyyppi]
  def hakulomakeLinkki: Kielistetty
  def lisatietoaHakeutumisesta: Kielistetty
  def lisatietoaValintaperusteista: Kielistetty
  def hakuaika: Option[Ajanjakso]
  def aloituspaikat: Option[Int]
  def aloituspaikkakuvaus: Kielistetty
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
    tyyppi: Koulutustyyppi = AmmTutkinnonOsa,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
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
    tyyppi: Koulutustyyppi = AmmOsaamisala,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    AmmatillinenMuuToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: amm-muu
    |              enum:
    |                - amm-muu
    |""")
case class AmmatillinenMuuToteutusMetadata(
    tyyppi: Koulutustyyppi = AmmMuu,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    YliopistoToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: yo
    |              enum:
    |                - yo
    |""")
case class YliopistoToteutusMetadata(
    tyyppi: Koulutustyyppi = Yo,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel("""    AmmattikorkeaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amk
    |              enum:
    |                - amk
    |""")
case class AmmattikorkeakouluToteutusMetadata(
    tyyppi: Koulutustyyppi = Amk,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel("""    AmmOpeErityisopeJaOpoToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-ope-erityisope-ja-opo
    |              enum:
    |                - amm-ope-erityisope-ja-opo
    |""")
case class AmmOpeErityisopeJaOpoToteutusMetadata(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel("""    OpePedagOpinnotToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: ope-pedag-opinnot
    |              enum:
    |                - ope-pedag-opinnot
    |""")
case class OpePedagOpinnotToteutusMetadata(
    tyyppi: Koulutustyyppi = OpePedagOpinnot,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

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
    |          description: Lukiodiplomin koodiUri. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/moduulikoodistolops2021/1).
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
    |            tyyppi:
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
    |              description: Lukio-toteutuksen painotukset. Taulukon alkioiden koodiUri-kentät viittaavat [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/lukiopainotukset/1).
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/LukiolinjaTieto'
    |            erityisetKoulutustehtavat:
    |              type: array
    |              description: Lukio-toteutuksen erityiset koulutustehtävät. Taulukon alkioiden koodiUri-kentät viittaavat [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/lukiolinjaterityinenkoulutustehtava/1).
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
    tyyppi: Koulutustyyppi = Lk,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    kielivalikoima: Option[Kielivalikoima],
    yleislinja: Boolean,
    painotukset: Seq[LukiolinjaTieto],
    erityisetKoulutustehtavat: Seq[LukiolinjaTieto],
    diplomit: Seq[LukiodiplomiTieto],
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel("""    TuvaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: tuva
    |              enum:
    |                - tuva
    |            jarjestetaanErityisopetuksena:
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
    jarjestetaanErityisopetuksena: Boolean,
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel("""    TelmaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: telma
    |              enum:
    |                - telma
    |""")
case class TelmaToteutusMetadata(
    tyyppi: Koulutustyyppi = Telma,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel("""    VapaaSivistystyoOpistovuosiToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
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
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false,
    liitetytOsaamismerkit: Seq[KoulutusOid] = Seq()
) extends ToteutusMetadata

@SwaggerModel("""    VapaaSivistystyoMuuToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
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
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false,
    liitetytOsaamismerkit: Seq[KoulutusOid] = Seq()
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    VapaaSivistystyoOsaamismerkkiToteutusMetadata:
                |      allOf:
                |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
                |        - type: object
                |          properties:
                |            tyyppi:
                |              type: string
                |              description: Koulutuksen metatiedon tyyppi
                |              example: vapaa-sivistystyo-osaamismerkki
                |              enum:
                |                - vapaa-sivistystyo-osaamismerkki
                |""")
case class VapaaSivistystyoOsaamismerkkiToteutusMetadata(
    tyyppi: Koulutustyyppi = VapaaSivistystyoOsaamismerkki,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false,
    suoritetaanNayttona: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    AikuistenPerusopetusToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: aikuisten-perusopetus
    |              enum:
    |                - aikuisten-perusopetus
    |""")
case class AikuistenPerusopetusToteutusMetadata(
    tyyppi: Koulutustyyppi = AikuistenPerusopetus,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel(
  """    KkOpintojaksoToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: kk-opintojakso
    |              enum:
    |                - kk-opintojakso
    |            isAvoinKorkeakoulutus:
    |              type: boolean
    |              description: Onko koulutus avointa korkeakoulutusta?
    |            tunniste:
    |              type: string
    |              description: Hakijalle näkyvä tunniste
    |            opinnonTyyppiKoodiUri:
    |              type: string
    |              description: Opinnon tyyppi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/html/koodisto/opinnontyyppi/1)
    |              example: opinnontyyppi_1#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusNumero:
    |              type: integer
    |              description: Opintojen laajuus tai kesto numeroarvona.
    |                HUOM! Syötettävissä vain kun koulutuksetKoodiUri-kenttään on valittu jokin seuraavista&#58; "koulutus_381501", "koulutus_381502", "koulutus_381503", "koulutus_381521". Muuten käytetään valitulta ePerusteelta (ePerusteId) tulevaa arvoa.
    |              example: 10
    |"""
)
case class KkOpintojaksoToteutusMetadata(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    kuvaus: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double] = None,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    isAvoinKorkeakoulutus: Option[Boolean],
    tunniste: Option[String] = None,
    opinnonTyyppiKoodiUri: Option[String] = None,
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    ErikoislaakariToteutusMetadata:
                |      allOf:
                |        - $ref: '#/components/schemas/ToteutusMetadata'
                |        - type: object
                |          properties:
                |            tyyppi:
                |              type: string
                |              description: Koulutuksen metatiedon tyyppi
                |              example: erikoislaakari
                |              enum:
                |                - erikoislaakari
                |""")
case class ErikoislaakariToteutusMetadata(
    tyyppi: Koulutustyyppi = Erikoislaakari,
    kuvaus: Kielistetty = Map(),
    opetus: Option[Opetus] = None,
    asiasanat: List[Keyword] = List(),
    ammattinimikkeet: List[Keyword] = List(),
    yhteyshenkilot: Seq[Yhteyshenkilo] = Seq(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends ToteutusMetadata

@SwaggerModel(
  """    KkOpintokokonaisuusToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: kk-opintokokonaisuus
    |              enum:
    |                - kk-opintokokonaisuus
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/opintojenlaajuusyksikko/1)
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusNumero:
    |              type: integer
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 10
    |            isAvoinKorkeakoulutus:
    |              type: boolean
    |              description: Onko koulutus avointa korkeakoulutusta?
    |            tunniste:
    |              type: string
    |              description: Hakijalle näkyvä tunniste
    |            opinnonTyyppiKoodiUri:
    |              type: string
    |              description: Opinnon tyyppi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/html/koodisto/opinnontyyppi/1)
    |              example: opinnontyyppi_1#1
    |"""
)
case class KkOpintokokonaisuusToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumero: Option[Double],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    isAvoinKorkeakoulutus: Option[Boolean],
    tunniste: Option[String] = None,
    opinnonTyyppiKoodiUri: Option[String] = None,
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false,
    liitetytOpintojaksot: Seq[ToteutusOid]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel(
  """    ErikoistumiskoulutusToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: erikoistumiskoulutus
    |              enum:
    |                - erikoistumiskoulutus
    |"""
)
case class ErikoistumiskoulutusToteutusMetadata(
    tyyppi: Koulutustyyppi = Erikoistumiskoulutus,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel(
  """    TaiteenPerusopetusToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: taiteen-perusopetus
    |              enum:
    |                - taiteen-perusopetus
    |            taiteenalaKoodiUrit:
    |              type: array
    |              description: Lista taiteenaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/taiteenperusopetustaiteenala/1)
    |              items:
    |                type: string
    |              example:
    |                - taiteenperusopetustaiteenala_kuvataide#1
    |                - taiteenperusopetustaiteenala_arkkitehtuuri#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/opintojenlaajuusyksikko/1)"
    |              example:
    |                - opintojenlaajuusyksikko_2#1
    |            opintojenLaajuusNumeroMin:
    |              type: integer
    |              description: Opintojen laajuuden vähimmäismäärä numeroarvona
    |              example: 10
    |            opintojenLaajuusNumeroMax:
    |              type: integer
    |              description: Opintojen laajuuden enimmäismäärä numeroarvona
    |              example: 20
    |"""
)
case class TaiteenPerusopetusToteutusMetadata(
    tyyppi: Koulutustyyppi = TaiteenPerusopetus,
    kuvaus: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    taiteenalaKoodiUrit: Seq[String],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel(
  """    MuuToteutusMetadata:
  |      allOf:
  |        - $ref: '#/components/schemas/ToteutusMetadata'
  |        - type: object
  |          properties:
  |            tyyppi:
  |              type: string
  |              description: Toteutuksen metatiedon tyyppi
  |              example: muu
  |              enum:
  |                - muu
  |            opintojenLaajuusyksikkoKoodiUri:
  |              type: string
  |              description: "Opintojen laajuusyksikko. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-app/koodisto/view/opintojenlaajuusyksikko/1)"
  |              example:
  |                - opintojenlaajuusyksikko_2#1
  |            opintojenLaajuusNumeroMin:
  |              type: integer
  |              description: Opintojen laajuuden vähimmäismäärä numeroarvona
  |              example: 10
  |            opintojenLaajuusNumeroMax:
  |              type: integer
  |              description: Opintojen laajuuden enimmäismäärä numeroarvona
  |              example: 20
  |"""
)
case class MuuToteutusMetadata(
    tyyppi: Koulutustyyppi = Muu,
    kuvaus: Kielistetty = Map(),
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumeroMin: Option[Double] = None,
    opintojenLaajuusNumeroMax: Option[Double] = None,
    opetus: Option[Opetus] = None,
    asiasanat: List[Keyword] = List(),
    ammattinimikkeet: List[Keyword] = List(),
    yhteyshenkilot: Seq[Yhteyshenkilo] = Seq(),
    isHakukohteetKaytossa: Option[Boolean] = None,
    hakutermi: Option[Hakutermi] = None,
    hakulomaketyyppi: Option[Hakulomaketyyppi] = None,
    hakulomakeLinkki: Kielistetty = Map(),
    lisatietoaHakeutumisesta: Kielistetty = Map(),
    lisatietoaValintaperusteista: Kielistetty = Map(),
    hakuaika: Option[Ajanjakso] = None,
    aloituspaikat: Option[Int] = None,
    aloituspaikkakuvaus: Kielistetty = Map(),
    hasJotpaRahoitus: Option[Boolean] = None,
    isTaydennyskoulutus: Boolean = false,
    isTyovoimakoulutus: Boolean = false
) extends TutkintoonJohtamatonToteutusMetadata
