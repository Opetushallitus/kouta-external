package fi.oph.kouta.external.kouta

import java.time.Instant
import java.util.concurrent.TimeUnit
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.{KoutaBackendJsonAdapter, KoutaJsonFormats}
import fi.oph.kouta.util.TimeUtils
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import org.asynchttpclient.{RequestBuilder, Response}
import io.netty.handler.codec.http.HttpHeaders
import org.json4s.Extraction.decompose
import org.json4s.jackson.JsonMethods.{parse, render, compact}
import org.json4s.jackson.Serialization.{read, write}
import fi.oph.kouta.logging.Logging
import org.json4s.JsonAST.{JNothing, JObject}

import scalaz.concurrent.Task
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.{Future, Promise}

object CasKoutaClient extends CasKoutaClient

class CasKoutaClient extends KoutaJsonFormats with KoutaBackendJsonAdapter with Logging with CallerId {

  def config = KoutaConfigurationFactory.configuration
  def clientConfig = config.clientConfiguration
  def urlProperties: OphProperties = config.urlProperties

  private val casConfig: CasConfig =
    new CasConfig.CasConfigBuilder(
      clientConfig.username,
      clientConfig.password,
      config.securityConfiguration.casUrl,
      urlProperties.url("kouta-backend.service"),
      callerId,
      callerId,
      "auth/login"
    ).setJsessionName("session").build()

  protected lazy val client: CasClient = CasClientBuilder.build(casConfig)

  def session(): Boolean = {
    val url = urlProperties.url("kouta-backend.session")
    val request = new RequestBuilder()
      .setMethod("GET")
      .setUrl(url)
      .build()
    val result = toScala(client.execute(request)).map {
      case r if r.getStatusCode() == 200 => true
      case r =>
        val body = r.getResponseBody()
        val status = r.getStatusCode()
        throw new RuntimeException(s"Url $url returned status $status with $body")
    }
    Await.result(result, 5.seconds)
  }

  def create[T](urlKey: String, body: T): Future[Either[(Int, String), IdResponse]] =
    toScala(client.execute(
      new RequestBuilder().setMethod("PUT").setUrl(urlProperties.url(urlKey)).build()
    )).map {
      case r if r.getStatusCode() == 200 =>
        Right(parse(r.getResponseBodyAsStream()) match {
          case obj: JObject if (obj \ "oid") != JNothing => obj.extract[OidResponse]
          case obj: JObject if (obj \ "id") != JNothing => obj.extract[UuidResponse]
        })
      case r => Left(r.getStatusCode(), r.getResponseBody())
    }

  def update[T](
      urlKey: String,
      body: T,
      ifUnmodifiedSince: Instant
  ): Future[Either[(Int, String), UpdateResponse]] =
    toScala(client.execute(
      new RequestBuilder()
        .setMethod("POST")
        .setUrl(urlProperties.url(urlKey))
        .setHeader(KoutaServlet.IfUnmodifiedSinceHeader, TimeUtils.renderHttpDate(ifUnmodifiedSince))
        .setHeader("Content-Type", "application/json")
        .build()
    )).map {
      case r if r.getStatusCode() == 200 =>
        Right(parse(r.getResponseBodyAsStream()).extract[UpdateResponse])
      case r => Left(r.getStatusCode(), r.getResponseBody())
    }

  protected def fetch[T](method: String, url: String, body: T, headers: HttpHeaders): Future[(Int, String)] =
    toScala(client.execute(
      new RequestBuilder()
        .setMethod(method)
        .setUrl(url)
        .setBody(compact(render(adaptToKoutaBackendJson(body, decompose(body)))))
        .setHeaders(headers)
        .build()
    )).map {
      r: Response => (r.getStatusCode(), r.getResponseBody())
    }

}
