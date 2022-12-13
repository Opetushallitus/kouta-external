package fi.oph.kouta.external

import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.mocks.{ServiceMockBase, SpecWithMocks}
import fi.oph.kouta.security.CasSession
import fi.oph.kouta.util.TimeUtils
import org.mockserver.matchers.MatchType

import java.time.Instant
import java.util.UUID

/* If you need to debug mocks,
   change log4j.logger.org.mockserver=INFO
   in test/resources/log4j.properties */

trait KoutaBackendMock extends SpecWithMocks with ServiceMockBase with KoutaJsonFormats {
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
