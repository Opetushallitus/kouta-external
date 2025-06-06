package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import co.elastic.clients.elasticsearch._types.{FieldSort, SortOptions, SortOrder, Time, query_dsl}
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch.core.{OpenPointInTimeRequest, search}
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference

import java.util.ArrayList
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.util

import co.elastic.clients.elasticsearch.core.SearchRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.{JavaClient, NoOpHttpClientConfigCallback}
import com.sksamuel.elastic4s.requests.get.GetResponse
import com.sksamuel.elastic4s._
import fi.oph.kouta.external.{ElasticSearchConfiguration, KoutaConfigurationFactory}
import fi.oph.kouta.logging.Logging
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder

import com.fasterxml.jackson.databind.DeserializationFeature
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.json4s.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.{ClassTag, classTag}
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport

trait ElasticsearchClient extends Logging {
  val index: String
  val client: ElasticClient
  val clientJava: elasticsearch.ElasticsearchClient
  val mapper: ObjectMapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule(DefaultScalaModule)

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  protected def getItem(id: String): Future[GetResponse] = {
    client.execute {
      val q = get(index, id)
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
  }

  protected def searchItems[T: ClassTag](queryList: util.List[query_dsl.Query]): List[T] = {
    val searchSize = 500
    try {
      val openPitRequest = new OpenPointInTimeRequest.Builder()
        .index(index)
        .keepAlive(new Time.Builder().time("1m").build())
        .build()
      val pointInTimeReference: PointInTimeReference = new PointInTimeReference.Builder()
        .keepAlive(new Time.Builder().time("1m").build())
        .id(clientJava.openPointInTime(openPitRequest).id())
        .build()
      val sortOpt =
        new SortOptions.Builder().field(FieldSort.of(f => f.field("oid.keyword").order(SortOrder.Asc))).build()
      val query = QueryBuilders.bool.must(queryList).build._toQuery()
      var searchRequestBuilder =
        new SearchRequest.Builder().query(query).size(searchSize).sort(sortOpt).pit(pointInTimeReference)

      val searchRequest = searchRequestBuilder.build()
      var response: co.elastic.clients.elasticsearch.core.SearchResponse[Map[String, Object]] =
        clientJava.search(searchRequest, classOf[Map[String, Object]])

      var hitList = new ArrayList[search.Hit[Map[String, Object]]]
      hitList.addAll(response.hits().hits())
      var hitCount = hitList.size()

      // Search rest of results (While hitCount equals searchSize there is more search results)
      while (hitCount == searchSize) {
        val lastHit     = response.hits().hits().last
        val lastHitSort = lastHit.sort()

        searchRequestBuilder = new SearchRequest.Builder()
          .query(query)
          .sort(sortOpt)
          .size(searchSize)
          .pit(pointInTimeReference)
          .searchAfter(lastHitSort)
        response = clientJava.search(searchRequestBuilder.build(), classOf[Object])
        hitList.addAll(response.hits().hits())
        hitCount = response.hits().hits().size()
      }

      val listToReturn =
        hitList.map(hit => mapper.convertValue(hit.source(), classTag[T].runtimeClass).asInstanceOf[T]).toList
      listToReturn
    } catch {
      case e: Exception =>
        logger.error("Got error: " + e.printStackTrace())
        List.empty
    }
  }

  private val debugJsonEnabled = false

  protected def debugJson(response: GetResponse): GetResponse = {
    if (debugJsonEnabled) logger.info(s"Elastic search response: ${response.sourceAsString}")
    response
  }
}
object ElasticsearchClient {
  val config: ElasticSearchConfiguration = KoutaConfigurationFactory.configuration.elasticSearchConfiguration;
  val httpClientConfigCallback: HttpClientConfigCallback = if (config.authEnabled) {
    lazy val provider = {
      val provider    = new BasicCredentialsProvider
      val credentials = new UsernamePasswordCredentials(config.username, config.password)
      provider.setCredentials(AuthScope.ANY, credentials)
      provider
    }
    (httpClientBuilder: HttpAsyncClientBuilder) => {
      httpClientBuilder.setDefaultCredentialsProvider(provider)
    }
  } else {
    NoOpHttpClientConfigCallback
  }
  val client: ElasticClient = ElasticClient(
    JavaClient(
      ElasticProperties(config.elasticUrl),
      (requestConfigBuilder: Builder) => {
        requestConfigBuilder
      },
      httpClientConfigCallback
    )
  )

  lazy val providerJavaClient = {
    val provider    = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(config.username, config.password)
    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }

  val mapper: ObjectMapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule(DefaultScalaModule)

  val restClient = RestClient
    .builder(HttpHost.create(config.elasticUrl))
    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
      def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
        httpClientBuilder.disableAuthCaching
        httpClientBuilder.setDefaultCredentialsProvider(providerJavaClient)
      }
    })
    .build()
  val transport: ElasticsearchTransport =
    new RestClientTransport(restClient, new JacksonJsonpMapper(ElasticsearchClient.mapper))
  val clientJava: co.elastic.clients.elasticsearch.ElasticsearchClient =
    new elasticsearch.ElasticsearchClient(transport)

}
