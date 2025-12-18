package fi.oph.kouta.external.domain.koutalight

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.domain.Kielistetty
import fi.oph.kouta.external.swagger.SwaggerModel

trait KoutaLightKoulutusBase {
  val externalId: String
  val kielivalinta: Seq[Kieli]
  val tila: String
  val nimi: Kielistetty
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
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        kuvaus:
    |          type: object
    |          description: Koulutuksen yleinen kuvaus eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |      required:
    |        - externalId
    |        - kielivalinta
    |        - tila
    |        - nimi
    |        - kuvaus
    |"""
)
case class KoutaLightKoulutus(
    externalId: String,
    kielivalinta: Seq[Kieli],
    tila: String,
    nimi: Kielistetty,
    kuvaus: Kielistetty
) extends KoutaLightKoulutusBase

case class KoutaLightKoulutusMetadata(
    kuvaus: Kielistetty
)
object KoutaLightKoulutusMetadata {
  def apply(koulutus: KoutaLightKoulutus): KoutaLightKoulutusMetadata = {
    KoutaLightKoulutusMetadata(
      koulutus.kuvaus
    )
  }
}

case class KoutaLightKoulutusWithMetadata(
    externalId: String,
    kielivalinta: Seq[Kieli],
    tila: String,
    nimi: Kielistetty,
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
      metadata,
      organisaatioOid
    )
  }
}
