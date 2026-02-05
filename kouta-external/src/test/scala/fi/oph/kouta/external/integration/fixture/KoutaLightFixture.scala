package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.external.service.KoutaLightService
import fi.oph.kouta.external.servlet.{KoutaLightServlet, KoutaLightSiirtotiedostoServlet}
import fi.oph.kouta.external.{KoutaConfigurationFactory, SiirtotiedostoPalveluClientMock}
import fi.oph.kouta.koutalight.domain.ExternalKoutaLightKoulutus
import fi.oph.kouta.koutalight.repository.{KoutaExternalDatabaseConnection, KoutaLightSiirtotiedostoDAO}
import fi.oph.kouta.koutalight.service.KoutaLightSiirtotiedostoService
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

trait KoutaLightFixture extends KoutaLightIntegrationSpec with AccessControlSpec {
  val KoutaLightPath               = "/koutan-tietomallista-poikkeavat-koulutukset/"
  val KoutaLightSiirtotiedostoPath = "/siirtotiedosto"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val koutaLightService = new KoutaLightService
    addServlet(new KoutaLightServlet(koutaLightService), KoutaLightPath)

    val dbConnectionConfiguration =
      KoutaConfigurationFactory.configuration.ovaraKoutaLightConfiguration.databaseConnectionConfiguration
    val dbConnection = KoutaExternalDatabaseConnection(dbConnectionConfiguration)

    val koutaLightSiirtotiedostoDAO           = new KoutaLightSiirtotiedostoDAO(dbConnection)
    val koutaLightSiirtotiedostoPalveluClient = new SiirtotiedostoPalveluClientMock()
    val koutaLightSiirtotiedostoService: KoutaLightSiirtotiedostoService =
      new KoutaLightSiirtotiedostoService(koutaLightSiirtotiedostoDAO, koutaLightSiirtotiedostoPalveluClient)

    addServlet(new KoutaLightSiirtotiedostoServlet(koutaLightSiirtotiedostoService), KoutaLightSiirtotiedostoPath)
  }

  def parseResult(result: String): JValue =
    parse(result)

  def put(sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    create(KoutaLightPath, List.empty, sessionId, expectedStatus, expectedBody)

  def put(koulutukset: List[ExternalKoutaLightKoulutus], sessionId: UUID): JValue =
    create(KoutaLightPath, koulutukset, sessionId, parseResult)

  def put(
      koulutukset: List[ExternalKoutaLightKoulutus],
      sessionId: UUID,
      expectedStatus: Int,
      expectedBody: String
  ): Unit =
    create(KoutaLightPath, koulutukset, sessionId, expectedStatus, expectedBody)

  def dateParam(dateTime: LocalDateTime): String = {
    val SiirtotiedostoDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    URLEncoder.encode(SiirtotiedostoDateTimeFormat.format(dateTime), StandardCharsets.UTF_8)
  }

  def get(
      entityPath: String,
      startTime: Option[LocalDateTime],
      endTime: Option[LocalDateTime],
      session: UUID,
      expectedStatusCode: Int
  ): String = {
    val queryParams = (startTime, endTime) match {
      case (Some(startTime), Some(endTime)) => s"?startTime=${dateParam(startTime)}&endTime=${dateParam(endTime)}"
      case (Some(startTime), None)          => s"?startTime=${dateParam(startTime)}"
      case (None, Some(endTime))            => s"?endTime=${dateParam(endTime)}"
      case (_, _)                           => ""
    }
    get(s"$KoutaLightSiirtotiedostoPath/$entityPath$queryParams", headers = Seq(sessionHeader(session))) {
      status should equal(expectedStatusCode)
      body
    }
  }
}
