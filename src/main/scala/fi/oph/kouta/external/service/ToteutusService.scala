package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ToteutusService extends ToteutusService(ToteutusClient, OrganisaatioServiceImpl)

class ToteutusService(toteutusClient: ToteutusClient, val organisaatioService: OrganisaatioService) extends RoleEntityAuthorizationService[Toteutus] with Logging {

  override val roleEntity: RoleEntity = Role.Toteutus

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
}
