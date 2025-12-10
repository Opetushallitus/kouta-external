package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.TestData.AmmToteutus
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.service.{ToteutusService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.MassToteutusServlet
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse

import java.util.UUID

trait MassToteutusFixture extends AccessControlSpec {
  this: KoutaIntegrationSpec =>

  val Path = "/toteutukset/"

  val toteutus: Toteutus       = AmmToteutus
  val toteutusOid: ToteutusOid = ToteutusOid("1.2.246.562.17.0000111")

  def toteutus(oid: ToteutusOid): Toteutus =
    toteutus.copy(oid = Some(oid))

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val toteutusService = new ToteutusService(
      new ToteutusClient(TempElasticClient.client, TempElasticClient.clientJava),
      new MockKoutaClient(urlProperties.get),
      organisaatioService
    )
    addServlet(new MassToteutusServlet(toteutusService), Path)
  }

  def parseResult(result: String): JValue =
    parse(result)

  def put(sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    create(Path, List.empty, sessionId, expectedStatus, expectedBody)

  def put(toteutukset: List[Toteutus]): JValue =
    create(Path, toteutukset, defaultSessionId, parseResult)

}
