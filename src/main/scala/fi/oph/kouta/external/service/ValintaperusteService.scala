package fi.oph.kouta.external.service

import java.util.UUID
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.external.kouta
import fi.oph.kouta.external.kouta.{CasKoutaClient, KoutaResponse, KoutaValintaperusteRequest, OidResponse, UuidResponse}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.RoleEntityAuthorizationService
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ValintaperusteService
    extends ValintaperusteService(ValintaperusteClient, CasKoutaClient, OrganisaatioServiceImpl)

class ValintaperusteService(
    valintaperusteClient: ValintaperusteClient,
    koutaClient: CasKoutaClient,
    val organisaatioService: OrganisaatioServiceImpl
) extends RoleEntityAuthorizationService[Valintaperuste]
    with Logging {

  override val roleEntity: RoleEntity = Role.Valintaperuste

  def get(id: UUID)(implicit authenticated: Authenticated): Future[Valintaperuste] =
    valintaperusteClient.getValintaperuste(id).map {
      authorizeGet(
        _,
        AuthorizationRules(
          roleEntity.readRoles.filterNot(_ == Indexer),
          allowAccessToParentOrganizations = true,
          Seq(AuthorizationRuleForJulkinen)
        )
      )
    }

  def create(valintaperuste: Valintaperuste)(implicit authenticated: Authenticated): Future[KoutaResponse[UUID]] = {
    koutaClient.create("kouta-backend.valintaperuste", KoutaValintaperusteRequest(authenticated, valintaperuste)).map {
      case Right(response: UuidResponse) => Right(response.id)
      case Right(response: OidResponse)  => Left((200, response.oid.s))
      case Left(x)                       => Left(x)
    }
  }

}
