package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.HakukohdeOid
import fi.oph.kouta.external.TestData.JulkaistuHakukohde
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.{HakuClient, HakukohdeClient}
import fi.oph.kouta.external.service.{HakuService, HakukohdeService, HakukohderyhmaService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.MassHakukohdeServlet
import fi.oph.kouta.external.{MockHakukohderyhmaClient, MockKoutaClient, TempElasticClient}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse

import java.util.UUID

trait MassHakukohdeFixture extends AccessControlSpec {
  this: KoutaIntegrationSpec =>

  val Path = "/hakukohteet"

  val hakukohde: Hakukohde = JulkaistuHakukohde
  val hakukohdeOid: HakukohdeOid = HakukohdeOid("1.2.246.562.17.0000111")

  def hakukohde(oid: HakukohdeOid): Hakukohde =
    hakukohde.copy(oid = Some(oid))

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService  = new OrganisaatioServiceImpl(urlProperties.get)
    val hakukohderyhmaClient = new MockHakukohderyhmaClient(urlProperties.get)

    val hakuService = new HakuService(
      new HakuClient(TempElasticClient.client, TempElasticClient.clientJava),
      new MockKoutaClient(urlProperties.get),
      organisaatioService
    )
    val hakukohderyhmaService = new HakukohderyhmaService(hakukohderyhmaClient, organisaatioService)
    val hakukohdeService = new HakukohdeService(
      new HakukohdeClient(TempElasticClient.client, TempElasticClient.clientJava),
      hakukohderyhmaService,
      new MockKoutaClient(urlProperties.get),
      organisaatioService,
      hakuService
    )
    addServlet(new MassHakukohdeServlet(hakukohdeService), Path)
  }

  def parseResult(result: String): JValue =
    parse(result)

  def put(sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    create(Path, List.empty, sessionId, expectedStatus, expectedBody)

  def put(hakukohteet: List[Hakukohde]): JValue =
    create(Path, hakukohteet, defaultSessionId, parseResult)

}
