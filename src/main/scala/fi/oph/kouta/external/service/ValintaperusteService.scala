package fi.oph.kouta.external.service

import java.util.UUID

import fi.oph.kouta.client.OrganisaatioClient
import fi.oph.kouta.external.client.OrganisaatioClientImpl
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.RoleEntityAuthorizationService
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ValintaperusteService extends ValintaperusteService(ValintaperusteClient, OrganisaatioClientImpl)

class ValintaperusteService(valintaperusteClient: ValintaperusteClient, val organisaatioClient: OrganisaatioClient)
  extends RoleEntityAuthorizationService[Valintaperuste] with Logging {

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
}
