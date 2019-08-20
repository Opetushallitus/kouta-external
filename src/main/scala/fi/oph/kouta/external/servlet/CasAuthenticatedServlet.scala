package fi.oph.kouta.external.servlet

import java.util.UUID

import fi.oph.kouta.external.security.{Authenticated, AuthenticationFailedException, SessionDAO}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet

trait CasAuthenticatedServlet {
  this: ScalatraServlet with Logging =>

  protected def authenticate: Authenticated = {
    val sessionCookie    = cookies.get("session")
    val sessionAttribute = Option(request.getAttribute("session")).map(_.toString)

    logger.trace("Session cookie {}", sessionCookie)
    logger.trace("Session attribute {}", sessionAttribute)

    val session = sessionCookie
      .orElse(sessionAttribute)
      .map(UUID.fromString)
      .flatMap(id => SessionDAO.get(id).map((id, _)))

    logger.trace("Session found {}", session)

    Authenticated.tupled(session.getOrElse(throw new AuthenticationFailedException))
  }
}