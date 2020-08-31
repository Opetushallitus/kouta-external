package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.elasticsearch.SorakuvausClient
import fi.oph.kouta.external.service.SorakuvausService
import fi.oph.kouta.external.servlet.SorakuvausServlet
import fi.oph.kouta.external.{KoutaFixtureTool, TempElasticClient}

trait SorakuvausFixture extends KoutaIntegrationSpec {
  val SorakuvausPath = "/sorakuvaus"

  addServlet(new SorakuvausServlet(new SorakuvausService(new SorakuvausClient(TempElasticClient.client))), SorakuvausPath)

  def get(id: UUID): Sorakuvaus = get[Sorakuvaus](SorakuvausPath, id)

  def get(id: UUID, sessionId: UUID): Sorakuvaus = get[Sorakuvaus](SorakuvausPath, id, sessionId)

  def get(id: UUID, sessionId: UUID, errorStatus: Int): Unit = get(s"$SorakuvausPath/$id", sessionId, errorStatus)

  def addMockSorakuvaus(id: UUID, organisaatioOid: OrganisaatioOid): Unit = {
    val sorakuvaus = KoutaFixtureTool.DefaultSorakuvausScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addSorakuvaus(id.toString, sorakuvaus)
    indexSorakuvaus(id)
  }
}
