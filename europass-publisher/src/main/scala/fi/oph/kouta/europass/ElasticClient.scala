package fi.oph.kouta.europass

import org.json4s._
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.{read, write}
import org.asynchttpclient.Dsl._
import org.asynchttpclient._

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed
import java.util.concurrent.CompletableFuture

abstract class Query

case class MatchQuery(`match`: Map[String, String]) extends Query

object MatchQuery {
  def apply(matchParams: (String, String)*) = new MatchQuery(matchParams.toMap)
}

trait SortOrder
case object Asc extends SortOrder

case class Search(
  query: Query,
  size: Integer,
  sort: Map[String, SortOrder],
  search_after: Option[List[String]]
)

trait ElasticClient extends Logging with KoutaJsonFormats {

  lazy val elasticUrl = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.url")
  lazy val username = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.username")
  lazy val password = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.password")
  lazy val realm = new Realm.Builder(username, password)
    .setUsePreemptiveAuth(true)
    .setScheme(Realm.AuthScheme.BASIC)
    .build()
  val httpClient = asyncHttpClient()

  def getJson(urlSuffix: String): JValue = {
    val req = get(s"${elasticUrl}/${urlSuffix}").setRealm(realm).build()
    val resp: Response = httpClient.executeRequest(req).toCompletableFuture().join()
    resp match {
      case r if r.getStatusCode == 200 => parse(r.getResponseBodyAsStream())
      case r => throw new RuntimeException(s"Elasticsearch query $urlSuffix failed: ${r.getResponseBody()}")
    }
  }

  def postJson[T <: AnyRef](urlSuffix: String, body: T): JValue = {
    val req: Request = post(s"${elasticUrl}/${urlSuffix}")
      .setRealm(realm)
      .setHeader("Content-type", "application/json")
      .setBody(write(body))
      .build()
    val resp: Response = httpClient.executeRequest(req).toCompletableFuture().join()
    resp match {
      case r if r.getStatusCode == 200 => parse(r.getResponseBodyAsStream())
      case r => throw new RuntimeException(s"Elasticsearch query $urlSuffix with "
        ++ s"body $body failed: ${r.getResponseBody()}")
    }
  }

  def getToteutus(oid: String): ToteutusIndexed =
    (getJson(s"toteutus-kouta/_doc/$oid") \ "_source").extract[ToteutusIndexed]

  def toteutusSearch(after: Option[String]) : Search =
    Search(
      query = MatchQuery("tila" -> "julkaistu"),
      size = 1000,
      sort = Map("oid.keyword" -> Asc),
      search_after = after.map(List(_))
    )

  def intoToteutusIndexedIfPossible(source: JValue): Option[ToteutusIndexed] =
    try {
      Some(source.extract[ToteutusIndexed])
    } catch {
      case e: MappingException => None
    }

  def listPublished(after: Option[String]): Stream[ToteutusIndexed] = {
    logger.info(s"listPublished: querying page after $after")
    val result = postJson("toteutus-kouta/_search", toteutusSearch(after))
    val hits: List[JValue] = (result \ "hits" \ "hits").children
    hits match {
      case Nil => Stream.empty
      // Stream.concat is eager in its second argument, #::: is truly lazy
      case _ => hits.map(_ \ "_source").flatMap(intoToteutusIndexedIfPossible).toStream #:::
        listPublished(Some((hits.last \ "sort")(0).extract[String]))
    }
  }
}

object ElasticClient extends ElasticClient
