package fi.oph.kouta.external.elasticsearch

import java.util.NoSuchElementException

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ElasticsearchClientHolder {
  def client: ElasticClient
}

object DefaultElasticsearchClientHolder extends ElasticsearchClientHolder {
  private lazy val elasticUrl: String = KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl

  private var clientHolder: Option[ElasticClient] = None

  def client: ElasticClient = clientHolder.getOrElse {
    clientHolder = Option(ElasticClient(ElasticProperties(elasticUrl)))
    clientHolder.orNull
  }
}

abstract class ElasticsearchClient(val index: String, val entityName: String, clientHolder: ElasticsearchClientHolder)
  extends Logging {

  lazy val elasticClient: ElasticClient = clientHolder.client

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  protected def getItem(id: String): Future[GetResponse] =
    elasticClient.execute {
      val q = get(id).from(index)
      logger.debug(s"Elasticsearch query: {}", q.show)
      q
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))

      case response: RequestSuccess[GetResponse] if !response.result.exists =>
        Future.failed(new NoSuchElementException(s"Didn't find $entityName with id $id"))

      case response: RequestSuccess[GetResponse] =>
        logger.debug(s"Elasticsearch status: {}", response.status)
        logger.debug(s"Elasticsearch response: {}", response.result.sourceAsString)
        Future.successful(response.result)
    }

  protected def simpleSearch(field: String, value: String): Future[SearchResponse] =
    elasticClient.execute {
      val q = search(index).query(matchPhraseQuery(field, value))
      logger.debug(s"Elasticsearch query: ${q.show}")
      q
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))

      case response: RequestSuccess[SearchResponse] if response.result.hits.isEmpty =>
        Future.failed(
          new NoSuchElementException(s"Didn't find anything searching for $entityName with $value in $field")
        )

      case response: RequestSuccess[SearchResponse] =>
        logger.debug(s"Elasticsearch status: {}", response.status)
        logger.debug(s"Elasticsearch response: [{}]", response.result.hits.hits.map(_.sourceAsString).mkString(","))
        Future.successful(response.result)
    }
}
