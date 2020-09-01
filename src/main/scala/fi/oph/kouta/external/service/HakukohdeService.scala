package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.HakukohdeOid
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.HakukohdeClient
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakukohdeService extends HakukohdeService(HakukohdeClient, OrganisaatioServiceImpl)

class HakukohdeService(hakukohdeClient: HakukohdeClient, val organisaatioService: OrganisaatioService) extends RoleEntityAuthorizationService[Hakukohde] with Logging {

  override val roleEntity: RoleEntity = Role.Hakukohde

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] = {

    hakukohdeClient.getHakukohde(oid).map {
      case (hakukohde, tarjoajat) =>
        val rules = AuthorizationRules(roleEntity.readRoles.filterNot(_ == Indexer), additionalAuthorizedOrganisaatioOids = tarjoajat)
        authorizeGet(hakukohde, rules)
    }
  }
}
