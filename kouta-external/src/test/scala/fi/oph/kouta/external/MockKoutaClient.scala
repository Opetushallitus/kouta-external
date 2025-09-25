package fi.oph.kouta.external

import fi.oph.kouta.external.kouta.CasKoutaClient
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.javautils.nio.cas.CasClient
import org.asynchttpclient.{AsyncHttpClient, Request}
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Dsl.asyncHttpClient
import java.util.concurrent.CompletableFuture
import java.util.{HashMap, Set}

object MockCasClient extends CasClient {
  private val client: AsyncHttpClient = asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().build())

  override def execute(request: Request) =
    client.executeRequest(request).toCompletableFuture()

  override def executeAndRetryWithCleanSessionOnStatusCodes(request: Request, codes: Set[Integer]) =
    client.executeRequest(request).toCompletableFuture()

  override def validateServiceTicketWithVirkailijaUsername(service: String, ticket: String) =
    CompletableFuture.completedFuture("valid")

  override def validateServiceTicketWithOppijaAttributes(service: String, ticket: String) =
    CompletableFuture.completedFuture(new HashMap())

}

class MockKoutaClient(mockProperties: OphProperties) extends CasKoutaClient {
  override def urlProperties = mockProperties

  override protected lazy val client: CasClient = MockCasClient
}
