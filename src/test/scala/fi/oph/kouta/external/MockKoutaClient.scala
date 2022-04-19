package fi.oph.kouta.external

import fi.oph.kouta.external.kouta.CasKoutaClient
import fi.vm.sade.properties.OphProperties
import org.http4s.client.Client
import org.http4s.client.blaze.defaultClient


class MockKoutaClient(mockProperties: OphProperties) extends CasKoutaClient {
  override def urlProperties = mockProperties

  override protected lazy val client: Client = defaultClient
}
