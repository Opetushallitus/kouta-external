package fi.oph.kouta.external.service

import java.util.UUID

import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.elasticsearch.HakuClient
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakuService extends HakuService(HakuClient)

class HakuService(hakuClient: HakuClient)
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
}
