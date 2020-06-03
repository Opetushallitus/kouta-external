package fi.oph.kouta.external.service

import java.time.Instant

import fi.oph.kouta.client.OrganisaatioClient
import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.external.client.OrganisaatioClientImpl
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.elasticsearch.HakuClient
import fi.oph.kouta.external.kouta.CasKoutaClient.KoutaResponse
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaClient, KoutaHakuRequest, UpdateResponse}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.RoleEntityAuthorizationService
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakuService extends HakuService(HakuClient, CasKoutaClient, OrganisaatioClientImpl)

class HakuService(val hakuClient: HakuClient, val koutaClient: KoutaClient, val organisaatioClient: OrganisaatioClient)
  extends RoleEntityAuthorizationService[Haku]
    with Logging {

  override val roleEntity: RoleEntity = Role.Haku
  protected val readRules: AuthorizationRules =
    AuthorizationRules(roleEntity.readRoles.filterNot(_ == Indexer), allowAccessToParentOrganizations = true)

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[(Haku, Instant)] =
    hakuClient.getHaku(oid).map(Some(_)).map(authorizeGet(_, readRules).get)

  /*
  def searchByAtaruId(ataruId: UUID)(implicit authenticated: Authenticated): Future[Seq[Haku]] = {
    val haut = hakuClient.searchByAtaruId(ataruId)

    if (hasRootAccess(roleEntity.readRoles)) {
      haut
    } else {
      withAuthorizedChildOrganizationOids(roleEntity.readRoles) { orgs =>
        haut.map(_.filter(h => orgs.exists(_ == h.organisaatioOid)))
      }
    }
  }
  */

  def create(haku: Haku)(implicit authenticated: Authenticated): Future[KoutaResponse[HakuOid]] = {
    koutaClient.createHaku(KoutaHakuRequest(authenticated, haku))
  }

  def update(haku: Haku, ifUnmodifiedSince: Instant)(
      implicit authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.updateHaku(KoutaHakuRequest(authenticated, haku), ifUnmodifiedSince)
}
