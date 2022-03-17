package fi.oph.kouta.external

import fi.oph.kouta.external.kouta.KoutaClient
import fi.vm.sade.javautils.nio.cas.CasClient
import fi.vm.sade.properties.OphProperties
import org.http4s.client.Client
import org.http4s.client.blaze.defaultClient
import org.mockito.Mockito


class MockKoutaClient(mockProperties: OphProperties) extends KoutaClient {
  override def urlProperties = mockProperties

  override protected lazy val client = Mockito.mock[CasClient](classOf[CasClient])
  override protected val loginParams: String = ""
  override protected val sessionCookieName: String = ""
}
