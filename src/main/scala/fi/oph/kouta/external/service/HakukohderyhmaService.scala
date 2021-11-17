package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.{HakukohdeOid, HakukohderyhmaOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.hakukohderyhmapalvelu.HakukohderyhmaClient
import fi.oph.kouta.security.{Authority, Role, RoleEntity}
import fi.oph.kouta.service.{
  OrganisaatioService,
  OrganizationAuthorizationFailedException,
  RoleEntityAuthorizationService
}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object HakukohderyhmaService extends HakukohderyhmaService(new HakukohderyhmaClient, OrganisaatioServiceImpl)

class HakukohderyhmaService(hakukohderyhmaClient: HakukohderyhmaClient, val organisaatioService: OrganisaatioService)
    extends RoleEntityAuthorizationService[Hakukohde]
    with Logging {

  def executor: ExecutionContext      = global
  override val roleEntity: RoleEntity = Role.Hakukohde
  private def authorityToHakukohderyhmaOid(authority: Authority): HakukohderyhmaOid = {
    HakukohderyhmaOid(authority.authority.split("_").last)
  }

  private def authorizeHakukohdeByHakukohderyhma(
      hakukohderyhmaOids: Seq[HakukohderyhmaOid],
      hakukohdeAuthorities: Seq[Authority],
      oid: HakukohdeOid
  ): Future[Boolean] = {
    val authorizedHakukohderyhmaOids = hakukohdeAuthorities
      .map(a => {
        val oid = authorityToHakukohderyhmaOid(a)
        if (hakukohderyhmaOids contains oid) Some(OrganisaatioOid(oid.toString())) else None
      })
      .filter(_.nonEmpty)
      .flatten
    if (authorizedHakukohderyhmaOids.nonEmpty) {
      logger.info(s"Hakukohde $oid was successfully authorized by hakukohderyhma(s) $authorizedHakukohderyhmaOids")
      Future.successful(true)
    } else Future.successful(false)
  }

  def authorizeHakukohde(oid: HakukohdeOid, hakukohde: Hakukohde)(implicit
      authenticated: Authenticated
  ): Future[Hakukohde] = {
    val hakukohdeAuthorities: Seq[Authority] =
      authenticated.session.authorities.filter(a => a.authority.startsWith("APP_KOUTA_HAKUKOHDE")).toSeq
    hakukohderyhmaClient
      .getHakukohderyhmat(oid)
      .flatMap(result => {
        authorizeHakukohdeByHakukohderyhma(result, hakukohdeAuthorities, oid).flatMap { authorized =>
          if (authorized) {
            Future.successful(hakukohde)
          } else {
            val errorString: String = s"User has no rights to hakukohde: $oid via hakukohderyhma roles."
            logger.warn(errorString)
            Future.failed(OrganizationAuthorizationFailedException(errorString))
          }
        }
      })
  }

  def getHakukohteetByHakukohderyhmaOid(
      oid: HakukohderyhmaOid
  )(implicit authenticated: Authenticated): Future[Seq[HakukohdeOid]] = {
    hakukohderyhmaClient.getHakukohteet(oid)
  }
  def getHakukohderyhmatByHakukohdeOid(
      oid: HakukohdeOid
  )(implicit authenticated: Authenticated): Future[Seq[HakukohderyhmaOid]] = {
    hakukohderyhmaClient.getHakukohderyhmat(oid)
  }
}
