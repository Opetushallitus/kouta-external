package fi.oph.kouta.external

import java.time.Instant
import java.util.UUID

import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.integration.KoutaBackendConverters
import fi.oph.kouta.external.security.CasSession
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.PortChecker
import org.json4s.jackson.Serialization.write
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.matchers.MatchType
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.Parameter.param
import org.mockserver.model.{Body, JsonBody}

import scala.io.Source

/* If you need to debug mocks,
   change log4j.logger.org.mockserver=INFO
   in test/resources/log4j.properties */

sealed trait ServiceMocks extends Logging with KoutaJsonFormats {

  var mockServer: Option[ClientAndServer]  = None
  var urlProperties: Option[OphProperties] = None

  def startServiceMocking(port: Int = PortChecker.findFreeLocalPort) = {
    logger.info(s"Mocking oph services in port $port")
    mockServer = Some(startClientAndServer(port))
    urlProperties = Some(
      KoutaConfigurationFactory.configuration.urlProperties
        .addOverride("host.virkailija", s"localhost:$port")
        .addOverride("host.kouta-backend", s"http://localhost:$port")
    )
  }

  def stopServiceMocking() = mockServer.foreach(_.stop())

  def clearServiceMocks() = mockServer.foreach(_.reset())

  protected def getMockPath(key: String) = {
    urlProperties.map(p => new java.net.URL(p.url(key)).getPath).getOrElse("/")
  }

  protected def responseFromResource(filename: String) =
    Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(s"data/$filename.json")).mkString

  protected def organisaationServiceParams(oid: OrganisaatioOid) =
    Map("oid" -> oid.s, "aktiiviset" -> "true", "suunnitellut" -> "true", "lakkautetut" -> "false")

  protected def mockGet(key: String, params: Map[String, String], responseString: String): Unit = {
    import scala.collection.JavaConverters._
    mockServer.foreach(
      _.when(
        request()
          .withMethod("GET")
          .withPath(getMockPath(key))
          .withQueryStringParameters(params.map(x => param(x._1, x._2)).toList.asJava)
      ).respond(
        response(responseString)
      )
    )
  }

  protected def mockCreate(key: String, json: AnyRef, responseStatus: Int, responseString: String): Unit =
    mockServer.foreach(
      _.when(
        request()
          .withMethod("PUT")
          .withPath(getMockPath(key))
          .withBody(JsonBody.json(write(json), MatchType.ONLY_MATCHING_FIELDS).asInstanceOf[Body[_]])
      ).respond(
        response().withBody(responseString).withStatusCode(responseStatus)
      )
    )

  protected def mockUpdate(
      key: String,
      json: AnyRef,
      ifUnmodifiedSince: Option[Instant],
      responseStatus: Int,
      responseString: String
  ): Unit =
    mockServer.foreach(
      _.when {
        val r = request()
          .withMethod("POST")
          .withPath(getMockPath(key))
          .withBody(JsonBody.json(write(json), MatchType.ONLY_MATCHING_FIELDS).asInstanceOf[Body[_]])

        ifUnmodifiedSince match {
          case None    => r
          case Some(i) => r.withHeader(KoutaServlet.IfUnmodifiedSinceHeader, KoutaServlet.renderHttpDate(i))
        }
      }.respond(
        response().withBody(responseString).withStatusCode(responseStatus)
      )
    )
}

trait KoutaBackendMock extends ServiceMocks {
  def authenticated(sessionId: UUID, session: CasSession) = Map("id" -> sessionId.toString, "session" -> session)

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

trait OrganisaatioServiceMock extends ServiceMocks {

  val OphOid            = OrganisaatioOid("1.2.246.562.10.00000000001")
  val ParentOid         = OrganisaatioOid("1.2.246.562.10.594252633210")
  val ChildOid          = OrganisaatioOid("1.2.246.562.10.81934895871")
  val EvilChildOid      = OrganisaatioOid("1.2.246.562.10.66634895871")
  val GrandChildOid     = OrganisaatioOid("1.2.246.562.10.67603619189")
  val EvilGrandChildOid = OrganisaatioOid("1.2.246.562.10.66603619189")
  val EvilCousin        = OrganisaatioOid("1.2.246.562.10.66634895666")

  val NotFoundOrganisaatioResponse = s"""{ "numHits": 0, "organisaatiot": []}"""
  lazy val DefaultResponse         = responseFromResource("organisaatio")

  def singleOidOrganisaatioResponse(oid: String) =
    s"""{ "numHits": 1, "organisaatiot": [{"oid": "$oid", "parentOidPath": "$oid/$OphOid", "children" : []}]}"""

  def mockOrganisaatioResponse(oid: OrganisaatioOid, response: String = DefaultResponse): Unit =
    mockGet("organisaatio-service.organisaatio.hierarkia", organisaationServiceParams(oid), response)

  def mockOrganisaatioResponses(oids: OrganisaatioOid*): Unit = oids.foreach(mockOrganisaatioResponse(_))

  def mockSingleOrganisaatioResponses(organisaatioOids: OrganisaatioOid*): Unit = organisaatioOids.foreach { oid =>
    mockOrganisaatioResponse(oid, singleOidOrganisaatioResponse(oid.s))
  }

  def mockSingleOrganisaatioResponses(first: String, organisaatioOids: String*): Unit =
    mockSingleOrganisaatioResponses((organisaatioOids :+ first).map(OrganisaatioOid): _*)
}

object OrganisaatioServiceMock extends OrganisaatioServiceMock
