package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.kouta.{KoulutusKoutaClient, KoutaKoulutusRequest, KoutaResponse}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KoulutusService extends KoulutusService(KoulutusClient, KoulutusKoutaClient, OrganisaatioServiceImpl)

class KoulutusService(koulutusClient: KoulutusClient, val koulutusKoutaClient: KoulutusKoutaClient, val organisaatioService: OrganisaatioService) extends RoleEntityAuthorizationService[Koulutus] with Logging {

  override val roleEntity: RoleEntity = Role.Koulutus

  def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] = {
    koulutusClient.getKoulutus(oid).map { koulutus =>
      authorizeGet(
        koulutus,
        AuthorizationRules(
          requiredRoles = roleEntity.readRoles.filterNot(_ == Indexer),
          allowAccessToParentOrganizations = true,
          overridingAuthorizationRules = Seq(AuthorizationRuleForJulkinen),
          additionalAuthorizedOrganisaatioOids = koulutus.tarjoajat
        )
      )
    }
  }

  def create(koulutus: Koulutus)(implicit authenticated: Authenticated): Future[KoutaResponse[KoulutusOid]] = {
    koulutusKoutaClient.createKoulutus(KoutaKoulutusRequest(authenticated, koulutus))
  }
}
