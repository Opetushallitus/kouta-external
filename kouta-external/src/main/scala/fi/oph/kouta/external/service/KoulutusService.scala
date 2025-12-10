package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaKoulutusRequest, KoutaResponse, OidResponse, UpdateResponse, UuidResponse}
import fi.oph.kouta.logging.Logging
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{AuthorizationRuleForReadJulkinen, AuthorizationRules, OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated

import java.time.Instant
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object KoulutusService extends KoulutusService(KoulutusClient, CasKoutaClient, OrganisaatioServiceImpl)

class KoulutusService(
    koulutusClient: KoulutusClient,
    val koutaClient: CasKoutaClient,
    val organisaatioService: OrganisaatioService
) extends RoleEntityAuthorizationService[Koulutus]
    with MassService[KoulutusOid, Koulutus]
    with Logging {

  override val roleEntity: RoleEntity = Role.Koulutus
  override val entityName: String     = "koulutus"

  def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] = {
    koulutusClient.getKoulutus(oid).map { koulutus =>
      authorizeGet(
        koulutus,
        AuthorizationRules(
          requiredRoles = roleEntity.readRoles.filterNot(_ == Indexer),
          allowAccessToParentOrganizations = true,
          overridingAuthorizationRule = Some(AuthorizationRuleForReadJulkinen),
          additionalAuthorizedOrganisaatioOids = koulutus.tarjoajat
        )
      )
    }(global)
  }

  def create(koulutus: Koulutus)(implicit authenticated: Authenticated): Future[KoutaResponse[KoulutusOid]] =
    koutaClient.create("kouta-backend.koulutus", KoutaKoulutusRequest(authenticated, koulutus)).map {
      case Right(response: OidResponse)  =>
        Right(KoulutusOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x)                       =>
        Left(x)
    }(global)

  def update(koulutus: Koulutus, ifUnmodifiedSince: Instant)(
    implicit authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.update("kouta-backend.koulutus", KoutaKoulutusRequest(authenticated, koulutus), ifUnmodifiedSince)

  override def createBlocking(koulutus: Koulutus)(implicit authenticated: Authenticated): KoutaResponse[KoulutusOid] =
    Await.result(create(koulutus), 60.seconds)

  override def updateBlocking(koulutus: Koulutus, ifUnmodifiedSince: Instant)(implicit
      authenticated: Authenticated
  ): KoutaResponse[UpdateResponse] =
    Await.result(update(koulutus, ifUnmodifiedSince), 60.seconds)
}
