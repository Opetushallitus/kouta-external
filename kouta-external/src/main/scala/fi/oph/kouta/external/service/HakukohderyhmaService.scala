package fi.oph.kouta.external.service

import com.github.blemale.scaffeine.Scaffeine
import fi.oph.kouta.domain.oid.{HakukohdeOid, HakukohderyhmaOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.hakukohderyhmapalvelu.HakukohderyhmaClient
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, OrganizationAuthorizationFailedException, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import fi.oph.kouta.external.KoutaConfigurationFactory

import scala.concurrent.duration.DurationLong

object HakukohderyhmaService extends HakukohderyhmaService(new HakukohderyhmaClient, OrganisaatioServiceImpl)

class HakukohderyhmaService(hakukohderyhmaClient: HakukohderyhmaClient, val organisaatioService: OrganisaatioService)
    extends RoleEntityAuthorizationService[Hakukohde]
    with Logging {

  private lazy val cache = Scaffeine()
    .expireAfterWrite(KoutaConfigurationFactory.configuration.hakukohderyhmaConfiguration.cacheTtlMinutes.minutes)
    .buildAsync[HakukohdeOid, Seq[HakukohderyhmaOid]]()

  def executor: ExecutionContext      = global
  override val roleEntity: RoleEntity = Role.Hakukohde

  def getAuthorizedHakukohderyhmaOids()(implicit authenticated: Authenticated): Set[HakukohderyhmaOid] = {
    authenticated.session.authorities
      .filter(a => a.authority.startsWith("APP_KOUTA_HAKUKOHDE"))
      .filter(a => HakukohderyhmaOid.apply(a.authority.split("_").last).isValid)
      .map(a => HakukohderyhmaOid(a.authority.split("_").last))
  }

  def authorizeHakukohde(oid: HakukohdeOid, hakukohde: Hakukohde)(implicit
      authenticated: Authenticated
  ): Future[Hakukohde] = {
    val authorizedHakukohderyhmas: Set[HakukohderyhmaOid] = getAuthorizedHakukohderyhmaOids

    getHakukohderyhmatByHakukohdeOid(oid)
      .flatMap(hakukohderyhmaOids => {
          if (hakukohderyhmaOids.toSet.intersect(authorizedHakukohderyhmas).nonEmpty) {
            logger.info(s"Hakukohde $oid was successfully authorized by hakukohderyhma(s) $authorizedHakukohderyhmas")
            Future.successful(hakukohde.withHakukohderyhmat(hakukohderyhmaOids))
          } else {
            val errorString: String = s"Authorization failed. User has no rights to hakukohde: $oid via hakukohderyhma roles."
            logger.warn(errorString)
            Future.failed(OrganizationAuthorizationFailedException(errorString))
          }
      })
  }

  def getHakukohteetByHakukohderyhmaOid(
      oid: HakukohderyhmaOid
  ): Future[Seq[HakukohdeOid]] = {
    hakukohderyhmaClient.getHakukohteet(oid)
  }
  def getHakukohderyhmatByHakukohdeOid(
      oid: HakukohdeOid
  ): Future[Seq[HakukohderyhmaOid]] = {
    cache.getFuture(oid, hakukohderyhmaClient.getHakukohderyhmat)
  }
}
