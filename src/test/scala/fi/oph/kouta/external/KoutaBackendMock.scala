package fi.oph.kouta.external

import java.time.Instant
import java.util.UUID
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.mocks.ServiceMocks
import fi.oph.kouta.security.CasSession
import fi.oph.kouta.util.TimeUtils
import fi.vm.sade.properties.OphProperties
import org.mockserver.matchers.MatchType
import org.scalatra.test.scalatest.ScalatraFlatSpec

/* If you need to debug mocks,
   change log4j.logger.org.mockserver=INFO
   in test/resources/log4j.properties */

trait KoutaBackendMock extends ScalatraFlatSpec with ServiceMocks with KoutaJsonFormats {
  var koutaBackendProperties: Option[OphProperties] = None

  override def beforeAll(): Unit = {
    super.beforeAll()
    super.startServiceMocking()
    urlProperties = Some(
      KoutaConfigurationFactory.configuration.urlProperties
        .addOverride("host.virkailija", s"localhost:$mockPort")
        .addOverride("host.kouta-backend", s"http://localhost:$mockPort")
    )
    koutaBackendProperties = urlProperties
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopServiceMocking()
  }

  def authenticated(sessionId: UUID, session: CasSession) = Map("id" -> sessionId.toString, "session" -> session)

  protected def addCreateMock(
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
      }.getOrElse(Seq()).toMap + ("entity" -> entity),
      statusCode = responseStatus,
      responseString = responseString,
      matchType = MatchType.ONLY_MATCHING_FIELDS
    )

  protected def addUpdateMock(
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
      }.getOrElse(Seq()).toMap + ("entity" -> entity),
      headers = headers,
      statusCode = responseStatus,
      responseString = responseString,
      matchType = MatchType.ONLY_MATCHING_FIELDS
    )
  }
}
