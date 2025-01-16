package fi.oph.kouta.europass

import org.json4s._
import org.json4s.jackson.JsonMethods.{parse, render, compact}
import org.asynchttpclient.Dsl._
import org.asynchttpclient._

import com.sksamuel.elastic4s.{ElasticClient => ScalaElasticClient}
import co.elastic.clients.elasticsearch._types.query_dsl.{Query, QueryBuilders, MatchQuery}
import co.elastic.clients.elasticsearch.{ElasticsearchClient => JavaElasticClient}

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed
import fi.oph.kouta.external.elasticsearch.ElasticsearchClient
import fi.oph.kouta.external.util.KoutaJsonFormats

import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

import scala.collection.immutable.Stream.concat
import scala.collection.JavaConverters._

object ElasticQueries {
  import org.json4s.JsonDSL._

  def toteutusByOid(oid: String): Query =
    QueryBuilders.bool.must(
      MatchQuery.of(q => q.field("oid.keyword").query(oid))._toQuery()
    ).build()._toQuery()

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

class ToteutusOps(
  val client: ScalaElasticClient,
  val clientJava: JavaElasticClient
) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "toteutus-kouta"
  def getToteutus(oid: String) =
    searchItems[ToteutusIndexed](List(ElasticQueries.toteutusByOid(oid)).asJava)
}

object ToteutusOps extends ToteutusOps(ElasticsearchClient.client, ElasticsearchClient.clientJava)

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

  def getToteutus(oid: String): List[ToteutusIndexed] = ToteutusOps.getToteutus(oid)

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
