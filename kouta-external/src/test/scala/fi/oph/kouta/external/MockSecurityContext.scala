package fi.oph.kouta.external

import fi.oph.kouta.external.kouta.CallerId
import fi.oph.kouta.external.security.{KayttooikeusUserDetails, SecurityContext}
import fi.oph.kouta.security.Authority
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import fi.vm.sade.javautils.nio.cas.impl.{CasClientImpl, CasSessionFetcher}
import org.asynchttpclient.Dsl.asyncHttpClient
import scalaz.concurrent.Task
import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.concurrent.duration.{Duration, SECONDS}

class MockSecurityContext(
    val casUrl: String,
    val casServiceIdentifier: String,
    users: Map[String, KayttooikeusUserDetails]
) extends SecurityContext with CallerId {

  val casConfig: CasConfig = new CasConfig.CasConfigBuilder("", "", "", "", callerId, callerId, "").build()
  val httpClient = asyncHttpClient()

  val casClient = new CasClientImpl(
    casConfig, httpClient,
    new CasSessionFetcher(
      casConfig, httpClient, Duration(20, TimeUnit.MINUTES).toMillis, Duration(2, TimeUnit.SECONDS).toMillis
    ) {

      override def fetchSessionToken(): CompletableFuture[String] =
        CompletableFuture.completedFuture("jsessionidFromMockSecurityContext")

  }) {
    override def validateServiceTicketWithVirkailijaUsername(service: String, serviceTicket: String): CompletableFuture[String] = {
      if (serviceTicket.startsWith(MockSecurityContext.ticketPrefix(service))) {
        val username = serviceTicket.stripPrefix(MockSecurityContext.ticketPrefix(service))
        CompletableFuture.completedFuture(username)
      } else {
        CompletableFuture.failedFuture(new RuntimeException("unrecognized ticket: " + serviceTicket))
      }
    }
  }
}

object MockSecurityContext {

  def apply(casUrl: String, casServiceIdentifier: String, defaultAuthorities: Set[Authority]): MockSecurityContext = {
    val users = Map("testuser" -> KayttooikeusUserDetails(defaultAuthorities, "mockoid"))

    new MockSecurityContext(casUrl, casServiceIdentifier, users)
  }

  def ticketFor(service: String, username: String): String = ticketPrefix(service) + username

  private def ticketPrefix(service: String): String = "mock-ticket-" + service + "-"
}
