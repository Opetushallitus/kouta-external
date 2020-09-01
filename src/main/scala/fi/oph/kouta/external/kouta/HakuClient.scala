package fi.oph.kouta.external.kouta

import java.time.Instant

import fi.oph.kouta.domain.oid.HakuOid

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait HakuClient {
  this: KoutaClient =>

  def createHaku(request: KoutaHakuRequest): Future[KoutaResponse[HakuOid]] = {
    val url = urlProperties.url("kouta-backend.haku")

    create(url, request).map {
      case Right(response: OidResponse)  => Right(HakuOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x)                       => Left(x)
    }
  }

  def updateHaku(request: KoutaHakuRequest, ifUnmodifiedSince: Instant): Future[KoutaResponse[UpdateResponse]] = {
    val url = urlProperties.url("kouta-backend.haku")
    update(url, request, ifUnmodifiedSince)
  }
}
