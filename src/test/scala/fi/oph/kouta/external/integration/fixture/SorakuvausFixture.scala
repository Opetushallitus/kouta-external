package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient}
import fi.oph.kouta.external.TestData.AmmSorakuvaus
import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.elasticsearch.SorakuvausClient
import fi.oph.kouta.external.service.{OrganisaatioServiceImpl, SorakuvausService}
import fi.oph.kouta.external.servlet.SorakuvausServlet

import java.time.Instant
import java.util.UUID

trait SorakuvausFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val SorakuvausPath = "/sorakuvaus"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val sorakuvausService = new SorakuvausService(new SorakuvausClient(TempElasticClient.client), new MockKoutaClient(urlProperties.get), organisaatioService)
    addServlet(new SorakuvausServlet(sorakuvausService), SorakuvausPath)
  }

  val sorakuvaus = AmmSorakuvaus

  def sorakuvaus(id: String): Sorakuvaus = sorakuvaus.copy(id = Some(UUID.fromString(id)))

  def sorakuvaus(organisaatioOid: OrganisaatioOid): Sorakuvaus =
    sorakuvaus.copy(organisaatioOid = organisaatioOid)

  def sorakuvaus(id: String, organisaatioOid: OrganisaatioOid): Sorakuvaus = {
    sorakuvaus.copy(id = Some(UUID.fromString(id)), organisaatioOid = organisaatioOid)
  }

  def get(id: UUID): Sorakuvaus = get[Sorakuvaus](SorakuvausPath, id)

  def get(id: UUID, sessionId: UUID): Sorakuvaus = get[Sorakuvaus](SorakuvausPath, id, sessionId)

  def get(id: UUID, sessionId: UUID, errorStatus: Int): Unit = get(s"$SorakuvausPath/$id", sessionId, errorStatus)

  def create(id: String, organisaatioOid: OrganisaatioOid): String =
    create(SorakuvausPath, sorakuvaus(id, organisaatioOid), parseId)

  def create(organisaatioOid: OrganisaatioOid, expectedStatus: Int, expectedBody: String): Unit =
    create(SorakuvausPath, sorakuvaus(organisaatioOid), defaultSessionId, expectedStatus, expectedBody)

  def create(organisaatioOid: OrganisaatioOid, sessionId: UUID): String =
    create(SorakuvausPath, sorakuvaus(organisaatioOid), sessionId, parseId)

  def update(id: String, ifUnmodifiedSince: Instant): Unit =
    update(SorakuvausPath, sorakuvaus(id), ifUnmodifiedSince)

  def update(id: String, ifUnmodifiedSince: Option[Instant], expectedStatus: Int, expectedBody: String): Unit =
    ifUnmodifiedSince match {
      case Some(ifUnmodifiedSinceVal) => update(SorakuvausPath, sorakuvaus(id), ifUnmodifiedSinceVal, defaultSessionId, expectedStatus, expectedBody)
      case _ => update(SorakuvausPath, sorakuvaus(id), defaultSessionId, expectedStatus, expectedBody)
    }

  def update(id: String, ifUnmodifiedSince: Instant, sessionId: UUID): Unit =
    update(SorakuvausPath, sorakuvaus(id), ifUnmodifiedSince, sessionId)

}
