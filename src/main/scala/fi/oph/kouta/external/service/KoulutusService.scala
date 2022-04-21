package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaKoulutusRequest, KoutaResponse, OidResponse, UuidResponse}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KoulutusService extends KoulutusService(KoulutusClient, CasKoutaClient, OrganisaatioServiceImpl)

class KoulutusService(koulutusClient: KoulutusClient, val koutaClient: CasKoutaClient, val organisaatioService: OrganisaatioService) extends RoleEntityAuthorizationService[Koulutus] with Logging {

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

  def create(koulutus: Koulutus)(implicit authenticated: Authenticated): Future[KoutaResponse[KoulutusOid]] =
    koutaClient.create("kouta-backend.koulutus", KoutaKoulutusRequest(authenticated, koulutus)).map {
      case Right(response: OidResponse)  =>
        Right(KoulutusOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x)                       =>
        Left(x)
    }
}
