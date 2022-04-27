package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.TempElasticClient
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.service.{OrganisaatioServiceImpl, ToteutusService}
import fi.oph.kouta.external.servlet.ToteutusServlet

import java.util.UUID

trait ToteutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val ToteutusPath = "/toteutus"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val toteutusService = new ToteutusService(new ToteutusClient(TempElasticClient.client), organisaatioService)
    addServlet(new ToteutusServlet(toteutusService), ToteutusPath)
  }

  def get(oid: ToteutusOid): Toteutus = get[Toteutus](ToteutusPath, oid)

  def get(oid: ToteutusOid, sessionId: UUID): Toteutus = get[Toteutus](ToteutusPath, oid, sessionId)

  def get(oid: ToteutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$ToteutusPath/$oid", sessionId, errorStatus)
}
