package fi.oph.kouta.external

import fi.oph.kouta.external.kouta.CallerId
import fi.oph.kouta.external.security.{AuthenticationFailedException, KayttooikeusUserDetails, SecurityContext}
import fi.oph.kouta.security.Authority
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig, UserDetails}
import fi.vm.sade.javautils.nio.cas.impl.{CasClientImpl, CasSessionFetcher}
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl.asyncHttpClient

import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.concurrent.duration.{Duration, SECONDS}
import scala.collection.JavaConverters._

class MockSecurityContext(
    val casUrl: String,
    val casServiceIdentifier: String,
    defaultAuthorities: Set[Authority]
) extends SecurityContext with CallerId {

  val casConfig: CasConfig = new CasConfig.CasConfigBuilder("", "", "", "", callerId, callerId, "").build()
  val httpClient: AsyncHttpClient = asyncHttpClient()

  val casClient: CasClient = new CasClientImpl(
    casConfig, httpClient,
    new CasSessionFetcher(
      casConfig, httpClient, Duration(20, TimeUnit.MINUTES).toMillis, Duration(2, TimeUnit.SECONDS).toMillis
    ) {

      override def fetchSessionToken(): CompletableFuture[String] =
        CompletableFuture.completedFuture("jsessionidFromMockSecurityContext")

  }) {
    override def validateServiceTicketWithVirkailijaUserDetails(service: String, serviceTicket: String): CompletableFuture[UserDetails] = {
      if (serviceTicket.startsWith(MockSecurityContext.ticketPrefix(service))) {
        val username: String = serviceTicket.stripPrefix(MockSecurityContext.ticketPrefix(service))
        if (username == "testuser") {
          val henkiloOid  = "test-user-oid"
          val roles       = defaultAuthorities.map(a => s"ROLE_${a.role}").asJava
          val userDetails = new UserDetails(username, henkiloOid, null, null, roles)
          CompletableFuture.completedFuture(userDetails)
        } else {
          CompletableFuture.failedFuture(new AuthenticationFailedException(s"User not found with username: $username"))
        }
      } else {
        CompletableFuture.failedFuture(new RuntimeException("unrecognized ticket: " + serviceTicket))
      }
    }
  }
}

object MockSecurityContext {

  def apply(casUrl: String, casServiceIdentifier: String, defaultAuthorities: Set[Authority]): MockSecurityContext = {
    new MockSecurityContext(casUrl, casServiceIdentifier, defaultAuthorities)
  }

  def ticketFor(service: String, username: String): String = ticketPrefix(service) + username

  private def ticketPrefix(service: String): String = "mock-ticket-" + service + "-"
}
