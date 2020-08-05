package fi.oph.kouta.external.service

import java.util.UUID

import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.elasticsearch.{ElasticsearchClientHolder, ValintaperusteClient}
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

class ValintaperusteService(elasticsearchClientHolder: ElasticsearchClientHolder)
  extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Valintaperuste

  val valintaperusteClient = new ValintaperusteClient("valintaperuste-kouta", elasticsearchClientHolder)

  def get(id: UUID)(implicit authenticated: Authenticated): Future[Valintaperuste] =
    authorizeGet(valintaperusteClient.getValintaperuste(id))

}
