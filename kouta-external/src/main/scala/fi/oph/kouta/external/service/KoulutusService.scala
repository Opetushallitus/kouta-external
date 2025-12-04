package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.enums.{MassResult, Operation}
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaKoulutusRequest, KoutaResponse, OidResponse, UpdateResponse, UuidResponse}
import fi.oph.kouta.external.servlet.MassOperations
import fi.oph.kouta.logging.Logging
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{AuthorizationRuleForReadJulkinen, AuthorizationRules, OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.oph.kouta.util.Timer

import java.time.Instant
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

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

  def massImport(koulutukset: List[Koulutus])
                (implicit authenticated: Authenticated): Future[List[MassResult]] = {
    implicit val executor: ExecutionContextExecutor = MassOperations.executor
    Future.traverse(koulutukset)(k => Future(handleKoulutusInMass(k)))
  }

  private def handleKoulutusInMass(koulutus: Koulutus)(implicit authenticated: Authenticated): MassResult =
    Timer.timed(s"Handling koulutus with oid ${koulutus.oid}") {
      logger.info(s"Processing koulutus: ${koulutus.oid}")
      koulutus.oid match {
        case None => Try(createBlocking(koulutus)) match {
          case Failure(e) =>
            logger.error(s"Mass create on koulutus threw an exception. Koulutus = $koulutus", e)
            MassResult.Error(Operation.Create, e)
          case Success(Left((status, message))) =>
            logger.warn(s"Creating koulutus failed. Response status = $status, message = $message. Koulutus = $koulutus")
            MassResult.Failure(Operation.Create, status, message)
          case Success(Right(oid: KoulutusOid)) =>
            logger.info(s"Created koulutus $oid")
            MassResult.CreateSuccess(oid)
        }
        case Some(oid) => Try(updateBlocking(koulutus, Instant.now())) match {
          case Failure(e) =>
            logger.error(s"Mass update on koulutus threw an exception. Koulutus = $koulutus", e)
            MassResult.Error(Operation.Update, e)
          case Success(Left((status, message))) =>
            logger.warn(s"Updating koulutus failed. Response status = $status, message = $message. Koulutus = $koulutus")
            MassResult.Failure(Operation.Update, status, message)
          case Success(Right(response)) =>
            logger.info(s"Updated koulutus $oid")
            MassResult.UpdateSuccess(response.updated)
        }
      }
    }

  private def createBlocking(koulutus: Koulutus)(implicit authenticated: Authenticated): KoutaResponse[KoulutusOid] =
    Await.result(create(koulutus), 60.seconds)

  private def updateBlocking(koulutus: Koulutus, ifUnmodifiedSince: Instant)
                            (implicit authenticated: Authenticated): KoutaResponse[UpdateResponse] =
    Await.result(update(koulutus, ifUnmodifiedSince), 60.seconds)
}
