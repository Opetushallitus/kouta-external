package fi.oph.kouta.external.service

import java.util.UUID

import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

object ValintaperusteService extends ValintaperusteService(ValintaperusteClient)

class ValintaperusteService(valintaperusteClient: ValintaperusteClient)
  extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Valintaperuste

  def get(id: UUID)(implicit authenticated: Authenticated): Future[Valintaperuste] =
    authorizeGet(valintaperusteClient.getValintaperuste(id))
}
