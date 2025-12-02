package fi.oph.kouta.external

import fi.oph.kouta.external.integration.fixture.KoutaIntegrationSpec
import fi.oph.kouta.external.kouta.CasKoutaClient
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.javautils.nio.cas.{CasClient, UserDetails}
import org.asynchttpclient.{AsyncHttpClient, DefaultAsyncHttpClientConfig, Request, Response}
import org.asynchttpclient.Dsl.asyncHttpClient

import java.util.concurrent.CompletableFuture
import java.util.{HashMap, Set}
import scala.collection.JavaConverters._

object MockCasClient extends CasClient {
  private val client: AsyncHttpClient = asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().build())

  override def execute(request: Request): CompletableFuture[Response] =
    client.executeRequest(request).toCompletableFuture

  override def executeAndRetryWithCleanSessionOnStatusCodes(request: Request, codes: Set[Integer]): CompletableFuture[Response] =
    client.executeRequest(request).toCompletableFuture

  override def validateServiceTicketWithVirkailijaUserDetails(service: String, ticket: String): CompletableFuture[UserDetails] = {
    val roles = KoutaIntegrationSpec.defaultAuthorities.map(_.role.toString())
    val userDetails = new UserDetails("testuser", "test-user-oids", null, null, roles.asJava)
    CompletableFuture.completedFuture(userDetails)
  }

  override def validateServiceTicketWithOppijaAttributes(service: String, ticket: String): CompletableFuture[HashMap[String, String]] =
    CompletableFuture.completedFuture(new HashMap())

}

class MockKoutaClient(mockProperties: OphProperties) extends CasKoutaClient {
  override def urlProperties: OphProperties = mockProperties

  override protected lazy val client: CasClient = MockCasClient
}
