package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.TestData.AmmKoulutus
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.service.{KoulutusService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.MassKoulutusServlet
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse

import java.util.UUID

trait MassKoulutusFixture extends AccessControlSpec {
  this: KoutaIntegrationSpec =>

  val Path = "/koulutukset/"

  val koulutus: Koulutus       = AmmKoulutus
  val koulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.123")

  def koulutus(oid: KoulutusOid): Koulutus =
    koulutus.copy(oid = Some(oid))

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val koulutusService = new KoulutusService(
      new KoulutusClient(TempElasticClient.client, TempElasticClient.clientJava),
      new MockKoutaClient(urlProperties.get),
      organisaatioService
    )
    addServlet(new MassKoulutusServlet(koulutusService), Path)
  }

  def parseResult(result: String): JValue =
    parse(result)

  def put(sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    create(Path, List.empty, sessionId, expectedStatus, expectedBody)

  def put(koulutukset: List[Koulutus]): JValue =
    create(Path, koulutukset, defaultSessionId, parseResult)

}
