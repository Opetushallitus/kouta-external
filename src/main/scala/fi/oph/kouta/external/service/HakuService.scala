package fi.oph.kouta.external.service

import java.util.UUID

import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.elasticsearch.{ElasticsearchClientHolder, HakuClient}
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuService(elasticsearchClientHolder: ElasticsearchClientHolder)
  extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Haku

  val hakuClient = new HakuClient("haku-kouta", elasticsearchClientHolder)

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
