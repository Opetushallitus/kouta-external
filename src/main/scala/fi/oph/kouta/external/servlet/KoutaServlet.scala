package fi.oph.kouta.external.servlet

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.{ConcurrentModificationException, NoSuchElementException}

import fi.oph.kouta.external.elasticsearch.ElasticSearchException
import fi.oph.kouta.external.security._
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization.write
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport

import scala.util.Try
import scala.util.control.NonFatal

trait KoutaServlet extends ScalatraServlet with KoutaJsonFormats with JacksonJsonSupport with Logging {
  import KoutaServlet._

  before() {
    contentType = formats("json")
  }

  protected def getIfUnmodifiedSince: Instant =
    request.headers.get(IfUnmodifiedSinceHeader) match {
      case Some(s) => parseIfUnmodifiedSince(s)
      case None    => throw new IllegalArgumentException(s"Otsake ${IfUnmodifiedSinceHeader} on pakollinen.")
    }

  def errorMsgFromRequest(): String = {
    def msgBody = request.body.length match {
      case x if x > 500000 => request.body.substring(0, 500000)
      case _               => request.body
    }

    s"Error ${request.getMethod} ${request.getContextPath} => $msgBody"
  }

  def badRequest(t: Throwable): ActionResult = {
    logger.warn(errorMsgFromRequest(), t)
    BadRequest("error" -> t.getMessage)
  }

  error {
    case e: AuthenticationFailedException =>
      logger.warn(s"authentication failed: ${e.getMessage}")
      Unauthorized("error" -> "Unauthorized")
    case e: RoleAuthorizationFailedException =>
      logger.warn("authorization failed", e.getMessage)
      Forbidden("error" -> "Forbidden")
    case e: OrganizationAuthorizationFailedException =>
      logger.warn("authorization failed", e.getMessage)
      Forbidden("error" -> s"Forbidden ${e.oids.mkString(", ")}")
    case e: IllegalStateException    => badRequest(e)
    case e: IllegalArgumentException => badRequest(e)
    case e: ConcurrentModificationException =>
      Conflict("error" -> e.getMessage)
    case e: NoSuchElementException =>
      NotFound("error" -> e.getMessage)
    case e: ElasticSearchException =>
      logger.error(s"Elasticsearch error: ${write(e.error)}")
      InternalServerError("error" -> "500 Internal Server Error")
    case NonFatal(e) =>
      logger.error(errorMsgFromRequest(), e)
      InternalServerError("error" -> "500 Internal Server Error")
  }
}

object KoutaServlet {
  val IfUnmodifiedSinceHeader = "x-If-Unmodified-Since"
  val LastModifiedHeader      = "x-Last-Modified"
  val SampleHttpDate: String  = renderHttpDate(Instant.EPOCH)

  def renderHttpDate(instant: Instant): String = {
    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.of("GMT")))
  }

  def parseHttpDate(string: String): Try[Instant] = Try {
    Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(string))
  }

  def parseIfUnmodifiedSince(value: String): Instant =
    parseHttpDate(value).recover {
      case e =>
        val msg = s"Ei voitu jäsentää otsaketta $IfUnmodifiedSinceHeader muodossa $SampleHttpDate."
        throw new IllegalArgumentException(msg, e)
    }.get
}
