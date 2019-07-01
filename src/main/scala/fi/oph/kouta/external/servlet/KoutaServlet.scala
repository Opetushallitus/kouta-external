package fi.oph.kouta.external.servlet

import java.text.ParseException
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.{ConcurrentModificationException, NoSuchElementException}

import fi.oph.kouta.external.util.fi.oph.kouta.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.MappingException
import org.scalatra.json.JacksonJsonSupport
import org.scalatra._

import scala.util.control.NonFatal
import scala.util.{Failure, Try}

trait KoutaServlet extends ScalatraServlet with KoutaJsonFormats with JacksonJsonSupport with Logging {

  before() {
    contentType = formats("json")
  }

  protected def renderHttpDate(instant: Instant): String = {
    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.of("GMT")))
  }

  protected def parseHttpDate(string: String): Instant = {
    Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(string))
  }

  protected def createLastModifiedHeader(instant: Instant): String = {
    //- system_time range in database is of form ["2017-02-28 13:40:02.442277+02",)
    //- RFC-1123 date-time format used in headers has no millis
    //- if Last-Modified/If-Unmodified-Since header is set to 2017-02-28 13:40:02, it will never be inside system_time range
    //-> this is why we wan't to set it to 2017-02-28 13:40:03 instead
    renderHttpDate(instant.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusSeconds(1))
  }

  val SampleHttpDate: String = renderHttpDate(Instant.EPOCH)

  protected def parseIfUnmodifiedSince: Option[Instant] = request.headers.get("If-Unmodified-Since") match {
    case Some(s) =>
      Try(parseHttpDate(s)) match {
        case x if x.isSuccess => Some(x.get)
        case Failure(e) => throw new IllegalArgumentException(s"Ei voitu jäsentää otsaketta If-Unmodified-Since muodossa $SampleHttpDate.", e)
      }
    case None => None
  }

  protected def getIfUnmodifiedSince: Instant = parseIfUnmodifiedSince match {
    case Some(s) => s
    case None => throw new IllegalArgumentException("Otsake If-Unmodified-Since on pakollinen.")
  }

  def errorMsgFromRequest(): String = {
    def msgBody = request.body.length match {
      case x if x > 500000 => request.body.substring(0, 500000)
      case _ => request.body
    }

    s"Error ${request.getMethod} ${request.getContextPath} => $msgBody"
  }

  def badRequest(t: Throwable): ActionResult = {
    logger.warn(errorMsgFromRequest(), t)
    BadRequest("error" -> t.getMessage)
  }

  error {
    case e: IllegalStateException => badRequest(e)
    case e: IllegalArgumentException => badRequest(e)
    case e: MappingException => badRequest(e)
    case e: ParseException => badRequest(e)
    case e: ConcurrentModificationException =>
      Conflict("error" -> e.getMessage)
    case e: NoSuchElementException =>
      NotFound("error" -> e.getMessage)
    case NonFatal(e) =>
      logger.error(errorMsgFromRequest(), e)
      InternalServerError("error" -> "500 Internal Server Error")
  }
}
