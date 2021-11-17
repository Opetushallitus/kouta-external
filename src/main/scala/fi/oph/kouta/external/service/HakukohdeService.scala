package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.HakukohdeClient
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object HakukohdeService
    extends HakukohdeService(HakukohdeClient, HakukohderyhmaService, OrganisaatioServiceImpl, HakuService)

class HakukohdeService(
    hakukohdeClient: HakukohdeClient,
    hakukohderyhmaService: HakukohderyhmaService,
    val organisaatioService: OrganisaatioService,
    hakuService: HakuService
) extends RoleEntityAuthorizationService[Hakukohde]
    with Logging  {
  def executor: ExecutionContext      = global
  override val roleEntity: RoleEntity = Role.Hakukohde

  def getHakukohdeAuthorizeByHakukohderyhma(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] = {
    hakukohdeClient.getHakukohde(oid).flatMap {
      case (hakukohde, _) =>
        hakukohderyhmaService.authorizeHakukohde(oid, hakukohde).flatMap {
          hakukohde: Hakukohde => Future.successful(hakukohde)
        }
    }
  }

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] = {
    hakukohdeClient.getHakukohde(oid).map {
      case (hakukohde, tarjoajat) =>
        val rules = AuthorizationRules(roleEntity.readRoles.filterNot(_ == Indexer), additionalAuthorizedOrganisaatioOids = tarjoajat)
        authorizeGet(hakukohde, rules)
    }
  }

  def search(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String], all: Boolean)(
      implicit authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    val tarjoajaOidsWithChilds = Some(
      tarjoajaOids.getOrElse(Set()).map(organisaatioService.getAllChildOidsFlat(_, false)).flatten
    )
    checkHakuExists.flatMap(_ => hakukohdeClient.search(hakuOid, if (all) None else tarjoajaOidsWithChilds, q))
  }
}
