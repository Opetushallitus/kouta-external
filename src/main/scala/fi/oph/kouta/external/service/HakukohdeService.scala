package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.HakukohdeClient
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakukohdeService extends HakukohdeService(HakukohdeClient, OrganisaatioServiceImpl, HakuService)

class HakukohdeService(hakukohdeClient: HakukohdeClient, val organisaatioService: OrganisaatioService, hakuService: HakuService) extends RoleEntityAuthorizationService[Hakukohde] with Logging {

  private val rootOrganisaatioOid = KoutaConfigurationFactory.configuration.securityConfiguration.rootOrganisaatio

  override val roleEntity: RoleEntity = Role.Hakukohde

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
    val checkHakuExists        = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    val tarjoajaOidsWithChilds = Some(tarjoajaOids.getOrElse(Set()).map(organisaatioService.getAllChildOidsFlat(_, false)).flatten)
    checkHakuExists.flatMap(_ =>
      hakukohdeClient.search(hakuOid, if (all) None else tarjoajaOidsWithChilds, q)
    )
  }
}
