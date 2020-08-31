package fi.oph.kouta.external.service

import java.time.Instant
import java.util.UUID

import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.elasticsearch.HakuClient
import fi.oph.kouta.external.kouta.CasKoutaClient.KoutaResponse
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaClient, KoutaHakuRequest, UpdateResponse}
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakuService extends HakuService(HakuClient, CasKoutaClient)

class HakuService(val hakuClient: HakuClient, val koutaClient: KoutaClient)
    extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Haku

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    authorizeGet(hakuClient.getHaku(oid))

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

  def create(haku: Haku)(implicit authenticated: Authenticated): Future[KoutaResponse[HakuOid]] =
    koutaClient.createHaku(KoutaHakuRequest(authenticated, haku))

  def update(haku: Haku, ifUnmodifiedSince: Instant)(
      implicit authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.updateHaku(KoutaHakuRequest(authenticated, haku), ifUnmodifiedSince)
}
