package fi.oph.kouta.external.hakukohderyhmapalvelu

import fi.oph.kouta.domain.oid.{HakukohdeOid, HakukohderyhmaOid}
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.kouta.CallerId
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.logging.Logging
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import org.asynchttpclient.RequestBuilder
import org.json4s.jackson.JsonMethods.parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.Future

import scala.concurrent.duration.{Duration, SECONDS}

object HakukohderyhmaClient

class HakukohderyhmaClient extends KoutaJsonFormats with CallerId with Logging {

  def config = KoutaConfigurationFactory.configuration
  def urlProperties: OphProperties = config.urlProperties

  private val casConfig: CasConfig =
    new CasConfig.CasConfigBuilder(
      config.clientConfiguration.username,
      config.clientConfiguration.password,
      config.securityConfiguration.casUrl,
      urlProperties.url("hakukohderyhmapalvelu.service"),
      callerId,
      callerId,
      "/auth/cas"
    ).setJsessionName("ring-session").build()

  protected lazy val client: CasClient = CasClientBuilder.build(casConfig)

  def getHakukohderyhmatWithoutRetry(oid: HakukohdeOid): Future[Seq[HakukohderyhmaOid]] = {
    val request = new RequestBuilder()
      .setMethod("GET")
      .setUrl(urlProperties.url("hakukohderyhmapalvelu.hakukohderyhmat", oid))
      .build()
    toScala(client.execute(request)).map {
      case r if r.getStatusCode() == 200 =>
        parse(r.getResponseBodyAsStream()).extract[Seq[String]].map(HakukohderyhmaOid)
      case r =>
        val body = r.getResponseBody()
        val status = r.getStatusCode()
        val error = s"Hakukohderyhmät fetch failed for hakukohdeoid: $oid with status $status, body: $body"
        throw new RuntimeException(error)
    }
  }

  def getHakukohderyhmat(oid: HakukohdeOid): Future[Seq[HakukohderyhmaOid]] = {
    getHakukohderyhmatWithoutRetry(oid).recoverWith {
      case e: RuntimeException =>
        logger.warn(e.getMessage() ++ ", retrying")
        getHakukohderyhmatWithoutRetry(oid)
    }
  }

  def getHakukohteet(oid: HakukohderyhmaOid): Future[Seq[HakukohdeOid]] = {
    val request = new RequestBuilder()
      .setMethod("GET")
      .setUrl(urlProperties.url("hakukohderyhmapalvelu.hakukohteet", oid))
      .build()
    toScala(client.execute(request)).map {
      case r if r.getStatusCode() == 200 =>
        parse(r.getResponseBodyAsStream()).extract[Seq[String]].map(HakukohdeOid)
      case r =>
        val body = r.getResponseBody()
        val status = r.getStatusCode()
        val error = s"Hakukohteet fetch failed for hakukohderyhmäoid: $oid with status $status, body: $body"
        throw new RuntimeException(error)
    }
  }

}
