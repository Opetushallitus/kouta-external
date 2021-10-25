package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.HitReader

import java.util.NoSuchElementException
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.search.{SearchIterator, SearchResponse}
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.searches.queries.Query
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.vm.sade.utils.Timer.timed
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.Serialization

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

object ElasticsearchClient {
  private val elasticUrl: String = KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl
  def client: ElasticClient = ElasticClient(ElasticProperties(elasticUrl))
}

trait ElasticsearchClient extends Logging {
  val index: String
  val client: ElasticClient

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  protected def getItem(id: String): Future[GetResponse] =
    client.execute {
      val q = get(id).from(index)
      logger.debug(s"Elasticsearch query: {}", q.show)
      q
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))

      case response: RequestSuccess[GetResponse] if !response.result.exists =>
        Future.failed(new NoSuchElementException(s"Didn't find id $id from $index"))

      case response: RequestSuccess[GetResponse] =>
        logger.debug(s"Elasticsearch status: {}", response.status)
        logger.debug(s"Elasticsearch response: {}", response.result.sourceAsString)
        Future.successful(response.result)
    }

  protected def simpleSearch(field: String, value: String): Future[SearchResponse] =
    client.execute {
      val q = search(index).query(matchPhraseQuery(field, value))
      logger.debug(s"Elasticsearch query: ${q.show}")
      q
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))

      case response: RequestSuccess[SearchResponse] if response.result.hits.isEmpty =>
        Future.failed(
          new NoSuchElementException(s"Didn't find anything searching for $value in $field from $index")
        )

      case response: RequestSuccess[SearchResponse] =>
        logger.debug(s"Elasticsearch status: {}", response.status)
        logger.debug(s"Elasticsearch response: [{}]", response.result.hits.hits.map(_.sourceAsString).mkString(","))
        Future.successful(response.result)
    }

  protected def searchItems[T: HitReader: ClassTag](query: Option[Query]): Future[IndexedSeq[T]] = {
    timed(s"SearchItems from ElasticSearch (Query: ${query}", 100) {
      implicit val duration: FiniteDuration = Duration(1, TimeUnit.MINUTES)

      query.fold[Future[IndexedSeq[T]]]({
        Future(
          SearchIterator.iterate[T](client, search(index).keepAlive("1m").size(500)).toIndexedSeq
        )
      })(q => {
        val request = search(index).bool(must(q)).keepAlive("1m").size(500)
        logger.info(s"Elasticsearch request: ${request.show}")
        Future {
          SearchIterator
            .hits(client, request)
            .toIndexedSeq
            .map(hit => hit.safeTo[T])
            .flatMap(entity =>
              entity match {
                case Success(value) => Some(value)
                case Failure(exception) =>
                  logger.error(
                    s"Unable to deserialize json response to entity: ",
                    exception
                  )
                  None
              }
            )
        }
      })
    }
  }

  private val debugJsonEnabled = false

  protected def debugJson(response: GetResponse): GetResponse = {
    if(debugJsonEnabled) logger.info(s"Elastic search response: ${response.sourceAsString}")
    response
  }
}
