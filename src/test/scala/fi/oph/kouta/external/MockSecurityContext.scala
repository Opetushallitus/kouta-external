package fi.oph.kouta.external

import fi.oph.kouta.external.kouta.CallerId
import fi.oph.kouta.external.security.{KayttooikeusUserDetails, SecurityContext}
import fi.oph.kouta.security.Authority
import fi.vm.sade.utils.cas.CasClient.{SessionCookie, Username}
import fi.vm.sade.utils.cas.{CasClient, CasParams}
import scalaz.concurrent.Task

class MockSecurityContext(
    val casUrl: String,
    val casServiceIdentifier: String,
    users: Map[String, KayttooikeusUserDetails]
) extends SecurityContext with CallerId {

  val casClient: CasClient = new CasClient("", null, "mockCallerId") {
    override def validateServiceTicketWithVirkailijaUsername(service: String)(serviceTicket: String): Task[Username] =
      if (serviceTicket.startsWith(MockSecurityContext.ticketPrefix(service))) {
        val username = serviceTicket.stripPrefix(MockSecurityContext.ticketPrefix(service))
        Task.now(username)
      } else {
        Task.fail(new RuntimeException("unrecognized ticket: " + serviceTicket))
      }

    override def fetchCasSession(params: CasParams, sessionCookieName: String): Task[SessionCookie] =
      Task.now("jsessionidFromMockSecurityContext")
  }
}

object MockSecurityContext {

  def apply(casUrl: String, casServiceIdentifier: String, defaultAuthorities: Set[Authority]): MockSecurityContext = {
    val users = Map("testuser" -> KayttooikeusUserDetails(defaultAuthorities, "mockoid"))

    new MockSecurityContext(casUrl, casServiceIdentifier, users)
  }

  def ticketFor(service: String, username: String): SessionCookie = ticketPrefix(service) + username

  private def ticketPrefix(service: String): SessionCookie = "mock-ticket-" + service + "-"
}
