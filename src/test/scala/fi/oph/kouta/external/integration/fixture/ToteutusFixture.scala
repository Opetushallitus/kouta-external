package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.{OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.external.TestData.AmmToteutus
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient, TestData}
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.service.{OrganisaatioServiceImpl, ToteutusService}
import fi.oph.kouta.external.servlet.ToteutusServlet

import java.util.UUID

trait ToteutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val ToteutusPath = "/toteutus"

  val toteutus = AmmToteutus

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val toteutusService = new ToteutusService(new ToteutusClient(TempElasticClient.client), new MockKoutaClient(urlProperties.get), organisaatioService)
    addServlet(new ToteutusServlet(toteutusService), ToteutusPath)
  }

  def toteutus(oid: String, organisaatioOid: OrganisaatioOid): Toteutus =
    toteutus.copy(oid = Some(ToteutusOid(oid)), organisaatioOid = organisaatioOid)

  def toteutus(organisaatioOid: OrganisaatioOid): Toteutus =
    toteutus.copy(organisaatioOid = organisaatioOid)

  def get(oid: ToteutusOid): Toteutus = get[Toteutus](ToteutusPath, oid)

  def get(oid: ToteutusOid, sessionId: UUID): Toteutus = get[Toteutus](ToteutusPath, oid, sessionId)

  def get(oid: ToteutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$ToteutusPath/$oid", sessionId, errorStatus)

  def create(toteutus: Toteutus): String =
    create(ToteutusPath, toteutus, parseOid)

  def create(toteutus: Toteutus, sessionId: UUID): String =
    create(ToteutusPath, toteutus, sessionId, parseOid)

}
