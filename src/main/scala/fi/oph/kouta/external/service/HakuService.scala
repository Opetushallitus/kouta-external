package fi.oph.kouta.external.service

import java.time.Instant
import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.elasticsearch.HakuClient
import fi.oph.kouta.external.kouta.CasKoutaClient.KoutaResponse
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaClient, KoutaHakuRequest, UpdateResponse}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakuService extends HakuService(HakuClient, CasKoutaClient, OrganisaatioServiceImpl)

class HakuService(val hakuClient: HakuClient, val koutaClient: KoutaClient, val organisaatioService: OrganisaatioService)
  extends RoleEntityAuthorizationService[Haku]
    with Logging {

  override val roleEntity: RoleEntity = Role.Haku
  protected val readRules: AuthorizationRules =
    AuthorizationRules(roleEntity.readRoles.filterNot(_ == Indexer), allowAccessToParentOrganizations = true)

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[(Haku, Instant)] =
    hakuClient.getHaku(oid).map(Some(_)).map(authorizeGet(_, readRules).get)

  def create(haku: Haku)(implicit authenticated: Authenticated): Future[KoutaResponse[HakuOid]] = {
    koutaClient.createHaku(KoutaHakuRequest(authenticated, haku))
  }

  def update(haku: Haku, ifUnmodifiedSince: Instant)(
      implicit authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.updateHaku(KoutaHakuRequest(authenticated, haku), ifUnmodifiedSince)

  def findByOids(hakuOids: Set[HakuOid])(implicit authenticated: Authenticated
  ): Future[Seq[Haku]] = {
    hakuClient.findByOids(hakuOids)
  }
}
