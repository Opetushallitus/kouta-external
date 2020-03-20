package fi.oph.kouta.external.service

import java.util.UUID

import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.elasticsearch.{ElasticsearchClientHolder, SorakuvausClient}
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

class SorakuvausService(elasticsearchClientHolder: ElasticsearchClientHolder)
  extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Valintaperuste

  val sorakuvausClient = new SorakuvausClient("sorakuvaus-kouta", elasticsearchClientHolder)

  def get(id: UUID)(implicit authenticated: Authenticated): Future[Sorakuvaus] =
    authorizeGet(sorakuvausClient.getSorakuvaus(id))

}
