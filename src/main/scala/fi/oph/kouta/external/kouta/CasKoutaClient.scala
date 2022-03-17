package fi.oph.kouta.external.kouta

import java.time.Instant
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.{KoutaJsonFormats, ScalaCasConfig}
import fi.oph.kouta.util.TimeUtils
import fi.vm.sade.javautils.nio.cas.{CasClient, CasConfig}
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.utils.slf4j.Logging
import org.asynchttpclient.{RequestBuilder, Response}
import org.http4s.{Header, Headers, Method}
import org.json4s.JsonAST.{JNothing, JObject}
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{Extraction, Writer}
import org.json4s.jackson.JsonMethods._

import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CasKoutaClient extends KoutaClient with CallerId {
  override protected val loginParams: String = "auth/login"
  override protected val sessionCookieName: String = "session"
}

object KoutaClient {

  type KoutaResponse[T] = Either[(Int, String), T]
}

abstract class KoutaClient extends KoutaJsonFormats with Logging with HakuClient with CallerId {


  protected def urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties

  protected val loginParams: String
  protected val sessionCookieName: String

  lazy protected val client: CasClient = {
    val config = KoutaConfigurationFactory.configuration.clientConfiguration
    new CasClient(ScalaCasConfig(
      config.username,
      config.password,
      urlProperties.url("cas.url"),
      urlProperties.url("kouta-backend.service"),
      callerId,
      callerId,
      loginParams,
      sessionCookieName)
    )
  }

  protected def create[T](url: String, body: T): Future[Either[(Int, String), IdResponse]] =
    fetch(Method.PUT, url, body, Headers.empty).map {
      case (200, body) =>
        Right(parse(body) match {
          case obj: JObject if (obj \ "id") != JNothing =>
            obj.extract[UuidResponse]
          case obj: JObject if (obj \ "oid") != JNothing =>
            obj.extract[OidResponse]
        })
      case (code, body) =>
        Left(code, body)
    }

  protected def update[T](
      url: String,
      body: T,
      ifUnmodifiedSince: Instant
  ): Future[Either[(Int, String), UpdateResponse]] = {
    val headers = Headers(
      Header(KoutaServlet.IfUnmodifiedSinceHeader, TimeUtils.renderHttpDate(ifUnmodifiedSince)),
      Header("Content-Type", "application/json")
    )
    fetch(Method.POST, url, body, headers).map {
      case (200, body) =>
        Right(parse(body).extract[UpdateResponse])
      case (code, body) =>
        Left(code, body)
    }
  }

  protected def fetch[T](method: Method, url: String, body: T, headers: Headers): Future[(Int, String)] = {
    implicit val writer: Writer[T] = (obj: T) => Extraction.decompose(obj)
    val requestBuilder = new RequestBuilder()
      .setMethod(method.name)
      .setUrl(url)
      .setBody(compact(render(writer.write(body))))
    headers.foreach(h => requestBuilder.addHeader(h.name, h.value))

    val request = requestBuilder.build()

    def responseToStatusAndBody(r: Response): (Int, String) = {
      (r.getStatusCode, r.getResponseBody)
    }

    val future: CompletableFuture[(Int, String)] = client.execute(request).thenApply(responseToStatusAndBody)
    toScala(future)
  }

}
