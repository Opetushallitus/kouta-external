package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaResponse, KoutaToteutusRequest, OidResponse, UpdateResponse, UuidResponse}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{AuthorizationRules, OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.oph.kouta.logging.Logging

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object ToteutusService extends ToteutusService(ToteutusClient, CasKoutaClient, OrganisaatioServiceImpl)

class ToteutusService(
    toteutusClient: ToteutusClient,
    val koutaClient: CasKoutaClient,
    val organisaatioService: OrganisaatioService
) extends RoleEntityAuthorizationService[Toteutus]
    with MassService[ToteutusOid, Toteutus]
    with Logging {

  override val roleEntity: RoleEntity = Role.Toteutus
  override val entityName: String     = "toteutus"

  def get(oid: ToteutusOid)(implicit authenticated: Authenticated): Future[Toteutus] =
    toteutusClient
      .getToteutus(oid)
      .map { t =>
        authorizeGet(
          t,
          AuthorizationRules(
            requiredRoles = roleEntity.readRoles.filterNot(_ == Indexer),
            allowAccessToParentOrganizations = true,
            additionalAuthorizedOrganisaatioOids = t.tarjoajat
          )
        )
      }

  def create(toteutus: Toteutus)(implicit authenticated: Authenticated): Future[KoutaResponse[ToteutusOid]] =
    koutaClient.create("kouta-backend.toteutus", KoutaToteutusRequest(authenticated, toteutus)).map {
      case Right(response: OidResponse)  =>
        Right(ToteutusOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x)                       =>
        Left(x)
    }

  def update(toteutus: Toteutus, ifUnmodifiedSince: Instant)(
    implicit authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.update("kouta-backend.toteutus", KoutaToteutusRequest(authenticated, toteutus), ifUnmodifiedSince)

  override def createBlocking(toteutus: Toteutus)(implicit authenticated: Authenticated): KoutaResponse[ToteutusOid] =
    Await.result(create(toteutus), 60.seconds)

  override def updateBlocking(toteutus: Toteutus, ifUnmodifiedSince: Instant)(implicit
      authenticated: Authenticated
  ): KoutaResponse[UpdateResponse] =
    Await.result(update(toteutus, ifUnmodifiedSince), 60.seconds)

}
