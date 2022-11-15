package fi.oph.kouta.external

import java.time.Instant
import java.util.UUID
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.mocks.{ServiceMocks}
import fi.oph.kouta.security.CasSession
import fi.oph.kouta.util.TimeUtils
import fi.vm.sade.properties.OphProperties
import org.mockserver.matchers.MatchType
import org.scalatra.test.scalatest.ScalatraFlatSpec

/* If you need to debug mocks,
   change log4j.logger.org.mockserver=INFO
   in test/resources/log4j.properties */

trait KoutaBackendMock extends ScalatraFlatSpec with ServiceMocks with KoutaJsonFormats {
  KoutaConfigurationFactory.setupWithDefaultTemplateFile()
  urlProperties = Some(KoutaConfigurationFactory.configuration.urlProperties)
  val koutaBackendProperties: Option[OphProperties] = urlProperties

  override def beforeAll(): Unit = {
    super.beforeAll()
    if (mockServer.isEmpty) {
      val virkailijaHostPort = urlProperties.get.getProperty("host.virkailija").split(":").last.toInt
      startServiceMocking(virkailijaHostPort)
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopServiceMocking()
  }

  def authenticated(sessionId: UUID, session: CasSession) = Map("id" -> sessionId.toString, "session" -> session)

  protected def addCreateMock(
      entityName: String,
      entity: AnyRef,
      pathKey: String,
      responseString: String,
      session: Option[(UUID, CasSession)] = None,
      responseStatus: Int = 200
  ): Unit =
    mockPut(
      path = getMockPath(pathKey),
      body = session.map { case (sessionId, session) =>
        Seq("authenticated" -> authenticated(sessionId, session))
      }.getOrElse(Seq()).toMap + (entityName -> entity),
      statusCode = responseStatus,
      responseString = responseString,
      matchType = MatchType.ONLY_MATCHING_FIELDS
    )

  protected def addUpdateMock(
      entityName: String,
      entity: AnyRef,
      pathKey: String,
      ifUnmodifiedSince: Option[Instant] = None,
      session: Option[(UUID, CasSession)] = None,
      responseString: String,
      responseStatus: Int = 200
  ): Unit = {
    val headers = ifUnmodifiedSince
      .map(i => KoutaServlet.IfUnmodifiedSinceHeader -> TimeUtils.renderHttpDate(i))
      .toMap + ("Content-Type" -> "application/json")

    mockPost(
      path = getMockPath(pathKey),
      body = session.map { case (sessionId, session) =>
        Seq("authenticated" -> authenticated(sessionId, session))
      }.getOrElse(Seq()).toMap + (entityName -> entity),
      headers = headers,
      statusCode = responseStatus,
      responseString = responseString,
      matchType = MatchType.ONLY_MATCHING_FIELDS
    )
  }
}
