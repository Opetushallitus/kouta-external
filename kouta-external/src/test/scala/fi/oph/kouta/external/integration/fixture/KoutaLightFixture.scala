package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.external.service.KoutaLightService
import fi.oph.kouta.external.servlet.KoutaLightServlet
import fi.oph.kouta.koutalight.domain.ExternalKoutaLightKoulutus
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse

import java.util.UUID

trait KoutaLightFixture extends KoutaLightIntegrationSpec with AccessControlSpec {

  val Path = "/koutan-tietomallista-poikkeavat-koulutukset/"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val koutaLightService = new KoutaLightService
    addServlet(new KoutaLightServlet(koutaLightService), Path)
  }

  def parseResult(result: String): JValue =
    parse(result)

  def put(sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    create(Path, List.empty, sessionId, expectedStatus, expectedBody)

  def put(koulutukset: List[ExternalKoutaLightKoulutus], sessionId: UUID): JValue =
    create(Path, koulutukset, sessionId, parseResult)

  def put(koulutukset: List[ExternalKoutaLightKoulutus], sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    create(Path, koulutukset, sessionId, expectedStatus, expectedBody)
}
