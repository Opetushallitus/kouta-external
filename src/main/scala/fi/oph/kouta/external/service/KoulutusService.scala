package fi.oph.kouta.external.service

import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.oid.KoulutusOid
import fi.oph.kouta.external.elasticsearch.{ElasticsearchClientHolder, KoulutusClient}
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KoulutusService(elasticsearchClientHolder: ElasticsearchClientHolder)
  extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Koulutus

  val koulutusClient = new KoulutusClient("koulutus-kouta", elasticsearchClientHolder)

  def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] = {
    koulutusClient.getKoulutus(oid).map {
      case koulutus if hasRootAccess(roleEntity.readRoles) =>
        koulutus
      case koulutus if koulutus.julkinen =>
        koulutus // TODO: sallittu vain saman koulutustyypin käyttäjille
      case koulutus =>
        withAuthorizedChildOrganizationOids(roleEntity.readRoles) { authorizedOrganizations =>
          authorize(koulutus.organisaatioOid, authorizedOrganizations) {
            koulutus
          }
        }
    }
  }
}
