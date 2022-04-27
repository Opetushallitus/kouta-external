package fi.oph.kouta.external.integration.fixture


import java.util.UUID
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.external.service.{OrganisaatioServiceImpl, ValintaperusteService}
import fi.oph.kouta.external.servlet.ValintaperusteServlet
import fi.oph.kouta.external.TempElasticClient

trait ValintaperusteFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val ValintaperustePath = "/valintaperuste"
  val ValintaperusteIdKey = "valintaperuste"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val valintaperusteService = new ValintaperusteService(new ValintaperusteClient(TempElasticClient.client), organisaatioService)
    addServlet(new ValintaperusteServlet(valintaperusteService), ValintaperustePath)
  }

  def get(id: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id)

  def get(id: UUID, sessionId: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id, sessionId)

  def get(id: UUID, sessionId: UUID, errorStatus: Int): Unit = get(s"$ValintaperustePath/$id", sessionId, errorStatus)

  def get(id: UUID, sessionId: UUID, expected: Valintaperuste): String =
    get[Valintaperuste, UUID](ValintaperusteIdKey, id, sessionId, expected)
}
