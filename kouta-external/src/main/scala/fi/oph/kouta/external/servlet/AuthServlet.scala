package fi.oph.kouta.external.servlet

import java.util.UUID

import fi.oph.kouta.external.security.CasSessionService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.security.ServiceTicket
import fi.vm.sade.javautils.nio.cas.CasLogout
import scala.compat.java8.OptionConverters._
import org.scalatra._

class AuthServlet(casSessionService: CasSessionService) extends KoutaServlet {

  def this() = this(CasSessionService)

  val casLogout = new CasLogout()

  override implicit val cookieOptions: CookieOptions = CookieOptions(
    path = "/kouta-external",
    secure = false,
    httpOnly = true
  )

  registerPath("/auth/login",
    """    get:
      |      summary: Kirjaudu sisään
      |      operationId: Kirjaudu sisaan
      |      description: Kirjaudu sisään
      |      tags:
      |        - Auth
      |      parameters:
      |        - in: query
      |          name: ticket
      |          schema:
      |            type: string
      |          required: true
      |          description: CAS-tiketti
      |      responses:
      |        '200':
      |          description: Ok
      |        '401':
      |          description: Unauthorized
      |""".stripMargin)
  get("/login") {
    val ticket = params.get("ticket").map(ServiceTicket)

    val existingSession = cookies
      .get("session")
      .orElse(Option(request.getAttribute("session")).map(_.toString))
      .map(UUID.fromString)

    casSessionService.getSession(ticket, existingSession) match {
      case Left(_) if ticket.isEmpty =>
        Found(s"${casSessionService.casUrl}/login?service=${casSessionService.serviceIdentifier}")
      case Left(t) => throw t
      case Right((id, session)) =>
        cookies += ("session" -> id.toString)
        request.setAttribute("session", id.toString)
        Ok(Map("personOid" -> session.personOid))
    }
  }

  registerPath("/auth/session",
    """    get:
      |      summary: Tarkista käyttäjän sessio
      |      operationId: Tarkista sessio
      |      description: Tarkista käyttäjän sessio
      |      tags:
      |        - Auth
      |      responses:
      |        '200':
      |          description: Ok
      |        '401':
      |          description: Unauthorized
      |""".stripMargin)
  get("/session") {
    val existingSession = cookies
      .get("session")
      .orElse(Option(request.getAttribute("session")).map(_.toString))
      .map(UUID.fromString)

    casSessionService.getSession(None, existingSession) match {
      case Left(t)             => throw t
      case Right((_, session)) => Ok(Map("personOid" -> session.personOid))
    }
  }

  registerPath("/auth/login",
    """    post:
      |      summary: Kirjaudu ulos
      |      operationId: Kirjaudu ulos
      |      description: Kirjaudu ulos
      |      tags:
      |        - Auth
      |      requestBody:
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              type: object
      |              properties:
      |                logoutRequest:
      |                  type: string
      |              required:
      |                - logoutRequest
      |      responses:
      |        '200':
      |          description: Ok
      |""".stripMargin)
  post("/login") {
    val logoutRequest = params
      .get("logoutRequest")
      .getOrElse(throw new IllegalArgumentException("Not 'logoutRequest' parameter given"))

    val ticket =
      toScala(casLogout.parseTicketFromLogoutRequest(logoutRequest))
      .getOrElse(throw new RuntimeException(s"Failed to parse CAS logout request $request"))

    casSessionService.deleteSession(ServiceTicket(ticket))
    NoContent()
  }
}
