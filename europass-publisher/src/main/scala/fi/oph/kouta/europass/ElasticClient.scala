package fi.oph.kouta.europass

import org.json4s._
import org.json4s.jackson.JsonMethods.{parse, render, compact}
import org.asynchttpclient.Dsl._
import org.asynchttpclient._

import fi.oph.kouta.logging.Logging
import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

import scala.collection.immutable.Stream.concat

object ElasticQueries {
  import org.json4s.JsonDSL._

  def toteutusSearch(after: Option[String]) : JValue = {
    val query = (("query" -> ("match" -> ("tila" -> "julkaistu"))) ~
                  ("size" -> 1000) ~
                  ("sort" -> ("oid.keyword" -> "asc")))
    after match {
      case None => query
      case Some(s) => query ~ ("search_after" -> List(after))
    }
  }
}

object ElasticClient extends Logging {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val formats = DefaultFormats

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

  def postJsonSync(urlSuffix: String, body: JValue) = {
    val req: Request = post(s"${elasticUrl}/${urlSuffix}")
      .setRealm(realm)
      .setHeader("Content-type", "application/json")
      .setBody(compact(render(body)))
      .build()
    val resp: Response = httpClient.executeRequest(req).toCompletableFuture().join()
    resp match {
      case r if r.getStatusCode == 200 => parse(r.getResponseBodyAsStream())
      case r => throw new RuntimeException(s"Elasticsearch query $urlSuffix with "
        ++ s"body $body failed: ${r.getResponseBody()}")
    }
  }

  def getToteutus(oid: String): Future[JValue] =
    getJson(s"toteutus-kouta/_doc/$oid").map{_ \ "_source"}

  def listPublished(after: Option[String]): Stream[JValue] = {
    logger.info(s"listPublished: querying page after $after")
    val result = postJsonSync("toteutus-kouta/_search", ElasticQueries.toteutusSearch(after))
    val hits: List[JValue] = (result \ "hits" \ "hits").children
    hits match {
      case Nil => Stream.empty
      case _ => concat(hits.map(_ \ "_source").toStream,
        listPublished(Some((hits.last \ "sort")(0).extract[String])))
    }
  }
}
