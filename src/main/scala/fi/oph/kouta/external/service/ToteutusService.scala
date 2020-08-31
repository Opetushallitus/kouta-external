package fi.oph.kouta.external.service

import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.domain.oid.ToteutusOid
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

object ToteutusService extends ToteutusService(ToteutusClient)

class ToteutusService(toteutusClient: ToteutusClient) extends RoleEntityAuthorizationService with Logging {

  override val roleEntity: RoleEntity = Role.Toteutus

  def get(oid: ToteutusOid)(implicit authenticated: Authenticated): Future[Toteutus] =
    authorizeGet(toteutusClient.getToteutus(oid))
}
