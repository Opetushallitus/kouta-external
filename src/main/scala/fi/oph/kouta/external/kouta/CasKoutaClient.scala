package fi.oph.kouta.external.kouta

import java.time.Instant
import java.util.concurrent.TimeUnit

import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.util.TimeUtils
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.defaultClient
import org.http4s.json4s.jackson.jsonEncoderOf
import org.json4s.JsonAST.{JNothing, JObject}
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{Extraction, Writer}
import scalaz.concurrent.Task

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}

object CasKoutaClient extends KoutaClient with CallerId {

  private def params = {
    val config = KoutaConfigurationFactory.configuration.clientConfiguration

    CasParams(
      urlProperties.url("kouta-backend.service"),
      "auth/login",
      config.username,
      config.password
    )
  }

  override lazy protected val client: Client = {
    CasAuthenticatingClient(
      new CasClient(KoutaConfigurationFactory.configuration.securityConfiguration.casUrl, defaultClient, callerId),
      casParams = params,
      serviceClient = defaultClient,
      clientCallerId = callerId,
      sessionCookieName = "session"
    )
  }
}

abstract class KoutaClient extends KoutaJsonFormats with Logging with HakuClient {

  type KoutaResponse[T] = Either[(Int, String), T]

  protected def urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties

  protected def client: Client

  def session(): Boolean = {
    val url = urlProperties.url("kouta-backend.session")

    Uri
      .fromString(url)
      .fold(
        Task.fail,
        url => {
          client.fetch(Request(Method.GET, url)) {
            case r if r.status.code == 200 =>
              Task.now(true)
            case r =>
              readStringBody(r).flatMap { body =>
                Task.fail(new RuntimeException(s"Url $url returned status code ${r.status} $body"))
              }
          }
        }
      )
      .unsafePerformSyncAttemptFor(Duration(5, TimeUnit.SECONDS))
      .fold(throw _, x => x)
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

  private def readStringBody(r: Response) = readBody[String](r)(s => s)

  private def readBody[A](r: Response)(handler: String => A) =
    r.bodyAsText.runLog
      .map(a => a.mkString)
      .map(
        response => handler(response)
      )

  protected def fetch[T](method: Method, url: String, body: T, headers: Headers): Future[(Int, String)] =
    Uri
      .fromString(url)
      .fold(
        Task.fail,
        url => {
          implicit val writer: Writer[T] = (obj: T) => Extraction.decompose(obj)
          client.fetch(Request(method, url, headers = headers).withBody(body)(jsonEncoderOf[T])) { r =>
            readBody[(Int, String)](r)(body => (r.status.code, body))
          }
        }
      )
      .runFuture()

  implicit class TaskExtensionOps[A](x: => Task[A]) {

    import scalaz.{-\/, \/-}

    def runFuture(): Future[A] = {
      val p: Promise[A] = Promise()
      x.unsafePerformAsync {
        case -\/(ex) =>
          p.failure(ex); ()
        case \/-(r) => p.success(r); ()
      }
      p.future
    }
  }

}
