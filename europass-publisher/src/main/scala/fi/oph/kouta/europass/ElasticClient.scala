package fi.oph.kouta.europass

import org.json4s._
import org.json4s.jackson.JsonMethods.parse
import org.asynchttpclient.Dsl._
import org.asynchttpclient._

import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

object ElasticClient {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  lazy val elasticUrl = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.url")
  lazy val username = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.username")
  lazy val password = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.password")
  lazy val realm = new Realm.Builder(username, password)
    .setUsePreemptiveAuth(true)
    .setScheme(Realm.AuthScheme.BASIC)
    .build()
  val httpClient = asyncHttpClient()

  def getJson(urlSuffix: String): Future[JValue] = {
    val req = get(s"${elasticUrl}/${urlSuffix}").setRealm(realm).build()
    toScala(httpClient.executeRequest(req).toCompletableFuture)
      .map {
        case r if r.getStatusCode == 200 => parse(r.getResponseBodyAsStream())
        case r => throw new RuntimeException(s"Elasticsearch query $urlSuffix failed: ${r.getResponseBody()}")
      }
  }

}
