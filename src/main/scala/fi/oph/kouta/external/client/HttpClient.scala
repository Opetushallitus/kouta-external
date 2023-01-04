package fi.oph.kouta.external.client

import java.util.{Map => JavaMap}
import fi.vm.sade.utils.http.DefaultHttpClient
import io.netty.handler.codec.http.cookie.DefaultCookie
import scalaj.http.HttpOptions._
import org.asynchttpclient.Dsl._

import java.util
import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

trait HttpClient extends CallerId {
  private val DefaultConnTimeout = 30000
  private val DefaultReadTimeout = 120000

  private def defaultOptions(doFollowRedirects: Boolean = false): Seq[HttpOption] = Seq(
    connTimeout(DefaultConnTimeout),
    readTimeout(DefaultReadTimeout),
    followRedirects(doFollowRedirects)
  )

  private val HeaderClientSubSystemCode = ("clientSubSystemCode", callerId)
  private val asyncClient = asyncHttpClient(
    config()
      .setReadTimeout(DefaultReadTimeout)
      .setConnectTimeout(DefaultConnTimeout)
  )

  def asyncGet[T](
      url: String,
      errorHandler: (String, Int, String) => Nothing = defaultErrorHandler,
      followRedirects: Boolean = true
  )(parse: String => T): Future[T] = {

    val v: CompletableFuture[T] = asyncClient
      .prepareGet(url)
      .setHeader("Caller-Id", callerId)
      .setFollowRedirect(followRedirects)
      .setHeader(HeaderClientSubSystemCode._1, HeaderClientSubSystemCode._2)
      .setHeader("CSRF", callerId)
      .setCookies(util.Arrays.asList(new DefaultCookie("CSRF", callerId)))
      .execute()
      .toCompletableFuture
      .thenApply(response => {
        response.getStatusCode match {
          case 200 =>
            parse(response.getResponseBody)
          case errorCode =>
            errorHandler(url, errorCode, response.getResponseBody)
        }
      })

    toScala(v)
  }

  def get[T](
      url: String,
      errorHandler: (String, Int, String) => Nothing = defaultErrorHandler,
      followRedirects: Boolean = true
  )(parse: String => T): T =
    DefaultHttpClient
      .httpGet(url, defaultOptions(followRedirects): _*)(callerId)
      .header(HeaderClientSubSystemCode._1, HeaderClientSubSystemCode._2)
      .responseWithHeaders match {
      case (200, _, response) => parse(response)
      case (xxx, _, response) => errorHandler(url, xxx, response)
    }
  private def defaultErrorHandler(url: String, statusCode: Int, response: String) =
    throw new RuntimeException(s"Url $url returned status code $statusCode $response")

  def toQueryParams(params: (String, String)*): JavaMap[String, String] =
    scala.collection.JavaConverters.mapAsJavaMap(Map(params: _*))
}
