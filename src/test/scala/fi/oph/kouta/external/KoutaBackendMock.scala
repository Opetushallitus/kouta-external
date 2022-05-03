package fi.oph.kouta.external

import fi.oph.kouta.domain.oid.OrganisaatioOid

import java.time.Instant
import java.util.UUID
import fi.oph.kouta.external.domain.{Haku, Hakukohde, Koulutus, Sorakuvaus, Toteutus, Valintaperuste}
import fi.oph.kouta.external.integration.KoutaBackendConverters
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
      path = getMockPath(key),
      body = json,
      headers = headers,
      statusCode = responseStatus,
      responseString = responseString,
      matchType = MatchType.ONLY_MATCHING_FIELDS
    )
  }

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

  private def addUpdateHakuMock(
      haku: Haku,
      ifUnmodifiedSince: Option[Instant] = None,
      session: Option[(UUID, CasSession)] = None,
      responseStatus: Int = 200,
      responseString: String = s"""{"updated": true}"""
  ): Unit =
    mockUpdate(
      key = "kouta-backend.haku",
      json = session.map { case (sessionId, session) =>
        Seq("authenticated" -> authenticated(sessionId, session))
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
