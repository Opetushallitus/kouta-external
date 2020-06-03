package fi.oph.kouta.external

import java.time.Instant
import java.util.UUID

import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.integration.KoutaBackendConverters
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.mocks.ServiceMocks
import fi.oph.kouta.security.CasSession
import fi.oph.kouta.util.TimeUtils
import org.mockserver.matchers.MatchType

/* If you need to debug mocks,
   change log4j.logger.org.mockserver=INFO
   in test/resources/log4j.properties */

trait KoutaBackendMock extends ServiceMocks with KoutaJsonFormats {
  def authenticated(sessionId: UUID, session: CasSession) = Map("id" -> sessionId.toString, "session" -> session)

  protected def mockUpdate(
      key: String,
      json: AnyRef,
      ifUnmodifiedSince: Option[Instant],
      responseStatus: Int,
      responseString: String
  ): Unit = {
    val headers = ifUnmodifiedSince
      .map(i => KoutaServlet.IfUnmodifiedSinceHeader -> TimeUtils.renderHttpDate(i))
      .toMap
    mockPost(
      key = key,
      body = json,
      headers = headers,
      statusCode = responseStatus,
      responseString = responseString,
      matchType = MatchType.ONLY_MATCHING_FIELDS
    )
  }

  private def addCreateHakuMock(
      haku: Haku,
      session: Option[(UUID, CasSession)] = None,
      responseStatus: Int = 200,
      responseString: String
  ): Unit =
    mockCreate(
      key = "kouta-backend.haku",
      json = session.map {
        case (sessionId, session) => Seq("authenticated" -> authenticated(sessionId, session))
      }.getOrElse(Seq()).toMap + ("haku" -> KoutaBackendConverters.convertHaku(haku)),
      responseString = responseString,
      responseStatus = responseStatus
    )

  def mockCreateHaku(haku: Haku, oid: String): Unit =
    addCreateHakuMock(haku, responseString = s"""{"oid": "$oid"}""")

  def mockCreateHaku(haku: Haku, oid: String, sessionId: UUID, session: CasSession): Unit =
    addCreateHakuMock(haku, Some((sessionId, session)), responseString = s"""{"oid": "$oid"}""")

  def mockCreateHaku(haku: Haku, responseStatus: Int, responseString: String): Unit =
    addCreateHakuMock(haku, responseStatus = responseStatus, responseString = responseString)

  protected def mockCreate(key: String, json: AnyRef, responseStatus: Int, responseString: String): Unit =
    mockPut(
      key = key,
      body = json,
      statusCode = responseStatus,
      responseString = responseString,
      matchType = MatchType.ONLY_MATCHING_FIELDS
    )

  private def addUpdateHakuMock(
      haku: Haku,
      ifUnmodifiedSince: Option[Instant] = None,
      session: Option[(UUID, CasSession)] = None,
      responseStatus: Int = 200,
      responseString: String = s"""{"updated": true}"""
  ): Unit =
    mockUpdate(
      key = "kouta-backend.haku",
      json = session.map {
        case (sessionId, session) => Seq("authenticated" -> authenticated(sessionId, session))
      }.getOrElse(Seq()).toMap + ("haku" -> KoutaBackendConverters.convertHaku(haku)),
      ifUnmodifiedSince,
      responseStatus,
      responseString
    )

  def mockUpdateHaku(haku: Haku, ifUnmodifiedSince: Instant): Unit =
    addUpdateHakuMock(haku, Some(ifUnmodifiedSince))

  def mockUpdateHaku(haku: Haku, ifUnmodifiedSince: Instant, sessionId: UUID, session: CasSession): Unit =
    addUpdateHakuMock(haku, Some(ifUnmodifiedSince), Some((sessionId, session)))

  def mockUpdateHaku(haku: Haku, ifUnmodifiedSince: Instant, responseStatus: Int, responseString: String): Unit =
    addUpdateHakuMock(haku, Some(ifUnmodifiedSince), responseStatus = responseStatus, responseString = responseString)

  def mockUpdateHaku(haku: Haku, responseStatus: Int, responseString: String): Unit =
    addUpdateHakuMock(haku, responseStatus = responseStatus, responseString = responseString)
}
