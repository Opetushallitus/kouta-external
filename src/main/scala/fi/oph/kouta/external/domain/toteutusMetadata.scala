package fi.oph.kouta.external.domain

import fi.oph.kouta.external.domain.enums.Koulutustyyppi
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
    |          description: Toteutuksen yhteyshenkilöiden tiedot
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
    |""")
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
    |""")
sealed trait KorkeakoulutusToteutusMetadata extends ToteutusMetadata {
  val alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
  val ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
}

@SwaggerModel(
  """    AmmatillinenToteutusMetadata:
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
    |""")
case class AmmatillinenToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisala],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadata

@SwaggerModel(
  """    YliopistoToteutusMetadata:
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

@SwaggerModel(
  """    AmmattikorkeaToteutusMetadata:
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

