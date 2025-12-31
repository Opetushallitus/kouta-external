package fi.oph.kouta.external.domain.koutalight

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.domain.{Keyword, Kielistetty, KielistettyLinkki}
import fi.oph.kouta.external.swagger.SwaggerModel

import java.net.URL
import java.time.LocalDateTime

trait KoutaLightKoulutusBase {
  val externalId: String
  val kielivalinta: Seq[Kieli]
  val tila: String
  val nimi: Kielistetty
  val tarjoajat: List[Kielistetty]
}

@SwaggerModel(
  """    KoutaLightKoulutus:
    |      type: object
    |      properties:
    |        externalId:
    |          type: string
    |          description: Koulutuksen yksilöllinen tunniste lähdejärjestelmässä.
    |          example: externalId1234
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joilla koulutuksen sisältötiedot on määritelty.
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          description: Koulutuksen julkaisutila.
    |        nimi:
    |          type: object
    |          description: Koulutuksen nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Nimi'
    |        tarjoajat:
    |          type: array
    |          description: Koulutusta tarjoavan organisaation nimi
    |          items:
    |            $ref: '#/components/schemas/Nimi'
    |        kuvaus:
    |          type: object
    |          description: Koulutuksen yleinen kuvaus eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        ammattinimikkeet:
    |          type: array
    |          description: Kokoelma ammattinimikkeitä, voi olla yksi tai useampi
    |          items:
    |            $ref: '#/components/schemas/KielistettyAmmattinimike'
    |        asiasanat:
    |          type: array
    |          description: Kokoelma asiasanoja, voi olla yksi tai useampi
    |          items:
    |            $ref: '#/components/schemas/KielistettyAsiasana'
    |        hakuaikaAlkaa:
    |          type: string
    |          format: date-time
    |          description: Haun alkamisajankohta
    |          example: 2025-08-23T09:00
    |        hakuaikaPaattyy:
    |          type: string
    |          format: date-time
    |          description: Haun päättymisajankohta
    |          example: 2025-08-30T15:00
    |        aloituspaikatLukumaara:
    |          type: integer
    |          description: Koulutuksen aloituspaikkojen lukumäärä
    |          example: 10
    |        hakulomakeLinkki:
    |          type: object
    |          description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |          $ref: '#/components/schemas/KielistettyLinkki'
    |        isTyovoimakoulutus:
    |          type: boolean
    |          description: Onko kyseessä työvoimakoulutus
    |          example: false
    |        johtaaTutkintoon:
    |          type: boolean
    |          description: Onko kyseessä tutkintoon johtava koulutus
    |          example: true
    |        maksullinen:
    |          type: boolean
    |          description: Onko koulutus maksullista
    |          example: true
    |        maksullisuuskuvaus:
    |          type: object
    |          description: Tarkempi kuvaus maksullisuudesta
    |          $ref: '#/components/schemas/Kuvaus'
    |        osaaminenUrit:
    |          type: array
    |          description: Koulutuksen tuottaman osaamisen ESCO-urit
    |          example: [http://data.europa.eu/esco/skill/c1f4e64a-9bc7-4c75-b2eb-d982d7148cef]
    |      required:
    |        - externalId
    |        - kielivalinta
    |        - tila
    |        - nimi
    |        - kuvaus
    |        - tarjoajat
    |"""
)
case class KoutaLightKoulutus(
    externalId: String,
    kielivalinta: Seq[Kieli],
    tila: String,
    nimi: Kielistetty,
    tarjoajat: List[Kielistetty],
    kuvaus: Kielistetty,
    ammattinimikkeet: List[Kielistetty] = List(),
    asiasanat: List[Kielistetty] = List(),
    hakuaikaAlkaa: Option[LocalDateTime] = None,
    hakuaikaPaattyy: Option[LocalDateTime] = None,
    aloituspaikatLukumaara: Option[Int] = None,
    hakulomakeLinkki: KielistettyLinkki = Map(),
    isTyovoimakoulutus: Boolean = false,
    johtaaTutkintoon: Boolean = false,
    maksullinen: Boolean = false,
    maksullisuuskuvaus: Kielistetty,
    osaaminenUrit: Seq[URL] = List()
) extends KoutaLightKoulutusBase

case class KoutaLightKoulutusMetadata(
    kuvaus: Kielistetty,
    ammattinimikkeet: List[Keyword],
    asiasanat: List[Keyword],
    hakuaikaAlkaa: Option[LocalDateTime],
    hakuaikaPaattyy: Option[LocalDateTime],
    aloituspaikatLukumaara: Option[Int],
    hakulomakeLinkki: KielistettyLinkki,
    isTyovoimakoulutus: Boolean,
    johtaaTutkintoon: Boolean,
    maksullinen: Boolean,
    maksullisuuskuvaus: Kielistetty,
    osaaminenUrit: Seq[URL]
)
object KoutaLightKoulutusMetadata {
  private def kielistettyToKeyword(kielistetty: Kielistetty) = for ((kieli, value) <- kielistetty)
    yield Keyword(kieli, value)

  def apply(koulutus: KoutaLightKoulutus): KoutaLightKoulutusMetadata = {
    new KoutaLightKoulutusMetadata(
      koulutus.kuvaus,
      koulutus.ammattinimikkeet.flatMap(kielistettyToKeyword),
      koulutus.asiasanat.flatMap(kielistettyToKeyword),
      koulutus.hakuaikaAlkaa,
      koulutus.hakuaikaPaattyy,
      koulutus.aloituspaikatLukumaara,
      koulutus.hakulomakeLinkki,
      koulutus.isTyovoimakoulutus,
      koulutus.johtaaTutkintoon,
      koulutus.maksullinen,
      koulutus.maksullisuuskuvaus,
      koulutus.osaaminenUrit
    )
  }
}

case class KoutaLightKoulutusWithMetadata(
    externalId: String,
    kielivalinta: Seq[Kieli],
    tila: String,
    nimi: Kielistetty,
    tarjoajat: List[Kielistetty],
    metadata: KoutaLightKoulutusMetadata,
    ownerOrg: OrganisaatioOid
) extends KoutaLightKoulutusBase

object KoutaLightKoulutusWithMetadata {
  def apply(organisaatioOid: OrganisaatioOid, koulutus: KoutaLightKoulutus): KoutaLightKoulutusWithMetadata = {
    val metadata = KoutaLightKoulutusMetadata(koulutus)
    new KoutaLightKoulutusWithMetadata(
      koulutus.externalId,
      koulutus.kielivalinta,
      koulutus.tila,
      koulutus.nimi,
      koulutus.tarjoajat,
      metadata,
      organisaatioOid
    )
  }
}
