package fi.oph.kouta.external.kouta

import fi.oph.kouta.domain.oid.KoulutusOid

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KoulutusKoutaClient extends KoulutusKoutaClient(CasKoutaClient) {

}

class KoulutusKoutaClient(val client: CasKoutaClient) {
  def createKoulutus(request: KoutaKoulutusRequest): Future[KoutaResponse[KoulutusOid]] = {
    val url = client.urlProperties.url("kouta-backend.koulutus")

    client.create(url, request).map {
      case Right(response: OidResponse)  =>
        Right(KoulutusOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x)                       =>
        Left(x)
    }
  }
}