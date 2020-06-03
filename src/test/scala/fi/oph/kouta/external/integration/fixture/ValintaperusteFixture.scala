package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.client.OrganisaatioClient
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.external.service.ValintaperusteService
import fi.oph.kouta.external.servlet.ValintaperusteServlet
import fi.oph.kouta.external.{KoutaConfigurationFactory, KoutaFixtureTool, TempElasticClient}

trait ValintaperusteFixture extends KoutaIntegrationSpec {
  val ValintaperustePath = "/valintaperuste"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioClient = new OrganisaatioClient(KoutaConfigurationFactory.configuration.urlProperties, "kouta-external")
    val valintaperusteService = new ValintaperusteService(new ValintaperusteClient(TempElasticClient.client), organisaatioClient)
    addServlet(new ValintaperusteServlet(valintaperusteService), ValintaperustePath)
  }

  def get(id: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id)

  def get(id: UUID, sessionId: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id, sessionId)

  def get(id: UUID, sessionId: UUID, errorStatus: Int): Unit = get(s"$ValintaperustePath/$id", sessionId, errorStatus)

  def addMockValintaperuste(
      id: UUID,
      organisaatioOid: OrganisaatioOid,
      sorakuvausId: UUID
  ): Unit = {
    val valintaperuste = KoutaFixtureTool.DefaultValintaperusteScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s) +
      (KoutaFixtureTool.SorakuvausIdKey -> sorakuvausId.toString)
    KoutaFixtureTool.addValintaperuste(id.toString, valintaperuste)
    indexValintaperuste(id)
  }

  def addMockSorakuvaus(id: UUID, organisaatioOid: OrganisaatioOid): Unit = {
    val sorakuvaus = KoutaFixtureTool.DefaultSorakuvausScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addSorakuvaus(id.toString, sorakuvaus)
    indexSorakuvaus(id)
  }
}
