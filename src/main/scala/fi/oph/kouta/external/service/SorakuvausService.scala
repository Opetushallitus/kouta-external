package fi.oph.kouta.external.service

import java.util.UUID

import fi.oph.kouta.client.OrganisaatioClient
import fi.oph.kouta.external.client.OrganisaatioClientImpl
import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.elasticsearch.SorakuvausClient
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.RoleEntityAuthorizationService
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SorakuvausService extends SorakuvausService(SorakuvausClient, OrganisaatioClientImpl)

class SorakuvausService(sorakuvausClient: SorakuvausClient, val organisaatioClient: OrganisaatioClient)
    extends RoleEntityAuthorizationService[Sorakuvaus]
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
            Seq(AuthorizationRuleForJulkinen)
          )
        )
      )
}
