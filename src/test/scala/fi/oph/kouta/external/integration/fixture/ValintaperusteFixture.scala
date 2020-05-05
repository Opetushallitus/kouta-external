package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.external.{KoutaFixtureTool, TempElasticClient}
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.external.service.ValintaperusteService
import fi.oph.kouta.external.servlet.ValintaperusteServlet

trait ValintaperusteFixture extends KoutaIntegrationSpec {
  val ValintaperustePath = "/valintaperuste"

  addServlet(new ValintaperusteServlet(new ValintaperusteService(new ValintaperusteClient(TempElasticClient.client))), ValintaperustePath)

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
