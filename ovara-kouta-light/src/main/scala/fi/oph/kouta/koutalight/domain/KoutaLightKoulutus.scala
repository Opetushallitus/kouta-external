package fi.oph.kouta.koutalight.domain

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid

import java.net.URL
import java.time.{Instant, LocalDateTime}
import java.util.UUID

trait KoutaLightKoulutusBase {
  val externalId: String
  val kielivalinta: Seq[Kieli]
  val tila: String
  val nimi: Kielistetty
  val tarjoajat: List[Kielistetty]
}

case class ExternalKoutaLightKoulutus(
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
    isMaksullinen: Boolean = false,
    maksullisuuskuvaus: Kielistetty = Map(),
    osaaminenUrit: Seq[URL] = List(),
    opetuskielet: Seq[String] = List()
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
    isMaksullinen: Boolean,
    maksullisuuskuvaus: Kielistetty,
    osaaminenUrit: Seq[URL],
    opetuskielet: Seq[String]
)
object KoutaLightKoulutusMetadata {
  private def kielistettyToKeyword(kielistetty: Kielistetty) = for ((kieli, value) <- kielistetty)
    yield Keyword(kieli, value)

  def apply(koulutus: ExternalKoutaLightKoulutus): KoutaLightKoulutusMetadata = {
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
      koulutus.isMaksullinen,
      koulutus.maksullisuuskuvaus,
      koulutus.osaaminenUrit,
      koulutus.opetuskielet
    )
  }
}

case class KoutaLightKoulutus(
    id: Option[UUID],
    externalId: String,
    kielivalinta: Seq[Kieli],
    tila: String,
    nimi: Kielistetty,
    tarjoajat: List[Kielistetty],
    metadata: KoutaLightKoulutusMetadata,
    ownerOrg: OrganisaatioOid,
    createdAt: Option[Instant],
    updatedAt: Option[Instant]
) extends KoutaLightKoulutusBase

object KoutaLightKoulutus {
  def apply(organisaatioOid: OrganisaatioOid, koulutus: ExternalKoutaLightKoulutus): KoutaLightKoulutus = {
    val metadata = KoutaLightKoulutusMetadata(koulutus)
    new KoutaLightKoulutus(
      None,
      koulutus.externalId,
      koulutus.kielivalinta,
      koulutus.tila,
      koulutus.nimi,
      koulutus.tarjoajat,
      metadata,
      organisaatioOid,
      None,
      None
    )
  }
}
