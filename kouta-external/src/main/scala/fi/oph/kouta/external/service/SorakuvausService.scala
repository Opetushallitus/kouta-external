package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.HakukohdeOid

import java.util.UUID
import fi.oph.kouta.external.domain.{Hakukohde, Sorakuvaus}
import fi.oph.kouta.external.elasticsearch.SorakuvausClient
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaHakukohdeRequest, KoutaResponse, KoutaSorakuvausRequest, OidResponse, UpdateResponse, UuidResponse}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{AuthorizationRuleByKoulutustyyppi, AuthorizationRules, OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.oph.kouta.logging.Logging

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SorakuvausService extends SorakuvausService(SorakuvausClient, CasKoutaClient, OrganisaatioServiceImpl)

class SorakuvausService(
    sorakuvausClient: SorakuvausClient,
    koutaClient: CasKoutaClient,
    val organisaatioService: OrganisaatioService
) extends RoleEntityAuthorizationService[Sorakuvaus]
    with Logging {

  override val roleEntity: RoleEntity = Role.Valintaperuste

  def get(id: UUID)(implicit authenticated: Authenticated): Future[Sorakuvaus] =
    sorakuvausClient
      .getSorakuvaus(id)
      .map(
        authorizeGet(
          _,
          AuthorizationRules(
            requiredRoles = roleEntity.readRoles.filterNot(_ == Indexer),
            allowAccessToParentOrganizations = true,
            Some(AuthorizationRuleByKoulutustyyppi)
          )
        )
      )

  def create(sorakuvaus: Sorakuvaus)(implicit authenticated: Authenticated): Future[KoutaResponse[UUID]] = {
    koutaClient.create("kouta-backend.sorakuvaus", KoutaSorakuvausRequest(authenticated, sorakuvaus)).map {
      case Right(response: UuidResponse) => Right(response.id)
      case Right(response: OidResponse)  => Left((200, response.oid.s))
      case Left(x)                       => Left(x)
    }
  }

  def update(sorakuvaus: Sorakuvaus, ifUnmodifiedSince: Instant)(
    implicit authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.update("kouta-backend.sorakuvaus", KoutaSorakuvausRequest(authenticated, sorakuvaus), ifUnmodifiedSince)
}
