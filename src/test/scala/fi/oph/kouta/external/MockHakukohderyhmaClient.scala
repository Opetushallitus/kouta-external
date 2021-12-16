package fi.oph.kouta.external

import fi.oph.kouta.domain.oid.{HakukohdeOid, HakukohderyhmaOid}
import fi.oph.kouta.external.hakukohderyhmapalvelu.HakukohderyhmaClient
import fi.vm.sade.properties.OphProperties
import org.http4s.client.Client
import org.http4s.client.blaze.defaultClient

import scala.concurrent.Future

class MockHakukohderyhmaClient(mockProperties: OphProperties) extends HakukohderyhmaClient {
  override def urlProperties                 = mockProperties
  override protected lazy val client: Client = defaultClient

  override def getHakukohderyhmat(oid: HakukohdeOid): Future[Seq[HakukohderyhmaOid]] = {
    if (oid == HakukohdeOid("1.2.246.562.20.00000000000000000015")) {
      Future.successful(
        Seq(
          HakukohderyhmaOid("1.2.246.562.28.00000000000000000015"),
          HakukohderyhmaOid("1.2.246.562.28.00000000000000001402"),
          HakukohderyhmaOid("1.2.246.562.28.00000000000000001401")
        )
      )
    } else
      Future.successful(
        Seq(
          HakukohderyhmaOid("1.2.246.562.28.00000000000000000002")
        )
      )
  }

  override def getHakukohteet(oid: HakukohderyhmaOid): Future[Seq[HakukohdeOid]] = {
    Future.successful(
      Seq(
        HakukohdeOid("1.2.246.562.20.00000000000000000115"),
        HakukohdeOid("1.2.246.562.20.00000000000000000116"),
        HakukohdeOid("1.2.246.562.20.00000000000000000117")
      )
    )
  }
}
