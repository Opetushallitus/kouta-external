package fi.oph.kouta.external.hakukohderyhmapalvelu

import fi.oph.kouta.domain.oid.{HakukohdeOid, HakukohderyhmaOid}
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.kouta.{CallerId, KoutaClient}
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import org.http4s.{Headers, Method}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohderyhmaClient extends KoutaClient with CallerId with KoutaJsonFormats with Logging {

  override protected val loginParams: String = "auth/cas"
  override protected val sessionCookieName: String  = "ring-session"
  override protected val serviceName: String = urlProperties.url("hakukohderyhmapalvelu.service")

  private implicit val formats = DefaultFormats

  def getHakukohderyhmat(oid: HakukohdeOid): Future[Seq[HakukohderyhmaOid]] = {
    fetch(Method.GET, urlProperties.url("hakukohderyhmapalvelu.hakukohderyhmat", oid), None, Headers.empty).flatMap {
      case (200, body) => Future.successful(parse(body).values.asInstanceOf[Seq[String]].map(s => HakukohderyhmaOid(s)))
      case (status, body) =>
        val errorString = s"Hakukohderyhmät fetch failed for hakukohdeoid: $oid with status $status, body: $body"
        logger.warn(errorString)
        fetch(Method.GET, urlProperties.url("hakukohderyhmapalvelu.hakukohderyhmat", oid), None, Headers.empty).flatMap {
          case (200, body) => Future.successful(parse(body).values.asInstanceOf[Seq[String]].map(s => HakukohderyhmaOid(s)))
          case (status, body) =>
            val errorString = s"Hakukohderyhmät fetch failed for hakukohdeoid: $oid with status $status, body: $body"
            logger.error(errorString)
            Future.failed(
              new RuntimeException(errorString)
            )
        }
    }
  }
  def getHakukohteet(oid: HakukohderyhmaOid): Future[Seq[HakukohdeOid]] = {
    fetch(Method.GET, urlProperties.url("hakukohderyhmapalvelu.hakukohteet", oid), None, Headers.empty).flatMap {
      case (200, body) => Future.successful(parse(body).values.asInstanceOf[Seq[String]].map(s => HakukohdeOid(s)))
      case (status, body) =>
        val errorString = s"Hakukohteet fetch failed for hakukohderyhmäoid: $oid with status $status, body: $body"
        logger.error(errorString)

        Future.failed(
          new RuntimeException(errorString)
        )
    }
  }


}
