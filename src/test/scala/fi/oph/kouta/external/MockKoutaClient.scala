package fi.oph.kouta.external

import fi.oph.kouta.external.kouta.KoutaClient
import org.http4s.client.Client
import org.http4s.client.blaze.defaultClient


class MockKoutaClient extends KoutaClient {
  override protected lazy val client: Client = defaultClient
}
