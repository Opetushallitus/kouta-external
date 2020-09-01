package fi.oph.kouta.external.servlet

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.{ConcurrentModificationException, NoSuchElementException}

import fi.oph.kouta.external.domain.Perustiedot
import fi.oph.kouta.external.elasticsearch.ElasticSearchException
import fi.oph.kouta.external.security._
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.service.{OrganizationAuthorizationFailedException, RoleAuthorizationFailedException}
import fi.oph.kouta.util.TimeUtils.renderHttpDate
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

  protected def createLastModifiedHeader[E <: Perustiedot[_, E]](entity: E): String = {
    // Oletetaan, että modified on Helsingin ajassa, kun sen mukana ei ole aikavyöhyketietoa
    val instant = entity.modified.get.atZone(ZoneId.of("Europe/Helsinki")).toInstant
    renderHttpDate(instant.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusSeconds(1))
  }

  protected def createLastModifiedHeader(instant: Instant): Map[String, String] = {
    //- system_time range in database is of form ["2017-02-28 13:40:02.442277+02",)
    //- RFC-1123 date-time format used in headers has no millis
    //- if x-Last-Modified/x-If-Unmodified-Since header is set to 2017-02-28 13:40:02, it will never be inside system_time range
    //-> this is why we wan't to set it to 2017-02-28 13:40:03 instead
    val value = renderHttpDate(instant.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusSeconds(1))
    Map(KoutaServlet.LastModifiedHeader -> value)
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
      logger.warn(s"authorization failed: ${e.getMessage}", e.getCause)
      Forbidden("error" -> s"Forbidden ${e.getMessage}")
    case e: IllegalStateException => badRequest(e)
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
