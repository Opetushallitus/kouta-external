package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.kouta.external.domain.indexed.{HakukohdeJavaClient, HakukohdeIndexedTest}
//import co.elastic.clients.elasticsearch._types.query_dsl
//import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery

import java.util
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.search.HitsMetadata
import com.fasterxml.jackson.databind.ObjectMapper
//import org.apache.lucene.search
//import org.elasticsearch.action.search.SearchRequest
import com.fasterxml.jackson.core.JsonParseException
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.{JavaClient, NoOpHttpClientConfigCallback}
import com.sksamuel.elastic4s.requests.get.GetResponse
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.requests.searches.{SearchIterator, SearchResponse}
import com.sksamuel.elastic4s._
import fi.oph.kouta.external.domain.indexed.HakukohdeIndexed
import jakarta.json.stream.JsonParsingException
import org.elasticsearch.action.search.{SearchAction, SearchRequestBuilder}
import org.elasticsearch.client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilders
import fi.oph.kouta.external.{ElasticSearchConfiguration, KoutaConfigurationFactory}
import fi.vm.sade.utils.Timer.timed
import fi.vm.sade.utils.slf4j.Logging
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.action
import co.elastic.clients.elasticsearch.core.GetRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import org.elasticsearch.client.{RequestOptions, RestClient}
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.json4s.Serialization

import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

// OY-4253
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport

trait ElasticsearchClient extends Logging {
  val index: String
  val client: ElasticClient

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  protected def getItem(id: String): Future[GetResponse] =
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

  //protected def searchItemsJavaClient[T: ClassTag](query: Option[Query]): Future[IndexedSeq[T]] = {
  protected def searchItemsJavaClient[T: ClassTag](query: Option[Query]): Unit = {
    logger.info(s"index = " + index)

    val srBuilder = new SearchRequest.Builder().index(index).size(100)
    val searchRequest = srBuilder.build()

    try {
      val newclient: co.elastic.clients.elasticsearch.ElasticsearchClient = createJavaClient
      // Tämä toimii, jos laittaa kiinteästi haettavan luokan (HakukohdeJavaClient)
      val response: co.elastic.clients.elasticsearch.core.SearchResponse[HakukohdeJavaClient] = newclient.search(searchRequest,classOf[HakukohdeJavaClient])

      // Tämä ei toimi
//      val response: co.elastic.clients.elasticsearch.core.SearchResponse[T] = newclient.search(searchRequest,classOf[T])


      val hitList = response.hits().hits()
      hitList.forEach(esResult =>
        logger.info("esResult.toResult() = " + esResult.source().toResult())
       )
      logger.info("List size = " + hitList.size())
//      logger.info("hitList.get(0).source() = " + hitList.get(0).source())
//      val esResult = hitList.get(0).source()
//      logger.info("esResult.toResult() = " + esResult.toResult());

      } catch {
      case e: JsonParsingException => println("Virhe: " + e.printStackTrace())
      case e: Exception => "Tämä virhe: " + e.printStackTrace()
      }



    /*
    timed(s"SearchItems from ElasticSearch (Query: ${query}", 100) {
      implicit val duration: FiniteDuration = Duration(1, TimeUnit.MINUTES)

      query.fold[Future[IndexedSeq[T]]]({
        Future(
          SearchIterator.iterate[T](client, search(index).keepAlive("1m").size(500)).toIndexedSeq
        )
      })(q => {
        val request2 = search(index).query(q).searchAfter
        logger.info(s"Elasticsearch request2: ${request2}")
        val request3 = search(index).query(q)
        logger.info(s"Elasticsearch request3: ${request3.show}")
        //  client.execute( search(index).query(q).sortBy("title").searchAfter(id) )
        val request = search(index).query(q).keepAlive("1m").size(500)
        logger.info(s"Elasticsearch request tässä: ${request.show}")
        logger.info("request.show jälkeen")

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

     */
  }


  /*
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
*/
  protected def searchItems[T: HitReader : ClassTag](query: Option[Query]): Future[IndexedSeq[T]] = {
    logger.info(s"query = " + query)
    timed(s"SearchItems from ElasticSearch (Query: ${query}", 100) {
      implicit val duration: FiniteDuration = Duration(1, TimeUnit.MINUTES)

      query.fold[Future[IndexedSeq[T]]]({
        Future(
          SearchIterator.iterate[T](client, search(index).keepAlive("1m").size(500)).toIndexedSeq
        )
      })(q => {
        val request = search(index).query(q).keepAlive("1m").size(500)
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
    if (debugJsonEnabled) logger.info(s"Elastic search response: ${response.sourceAsString}")
    response
  }
  protected def createJavaClient : co.elastic.clients.elasticsearch.ElasticsearchClient = {
    val config: ElasticSearchConfiguration = KoutaConfigurationFactory.configuration.elasticSearchConfiguration;
    lazy val provider2 = {
      val provider = new BasicCredentialsProvider
      val credentials = new UsernamePasswordCredentials(config.username, config.password)
      provider.setCredentials(AuthScope.ANY, credentials)
      provider
    }
    val clientJava = RestClient.builder(
        new HttpHost("pallero-opintopolku.es.eu-west-1.aws.found.io", 9243, "https"))
      .setHttpClientConfigCallback(new HttpClientConfigCallback() {
        def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
          httpClientBuilder.disableAuthCaching
          httpClientBuilder.setDefaultCredentialsProvider(provider2)
        }
      }).build()
    // Create the transport with a Jackson mapper
    val objectMapper: ObjectMapper = new JacksonJsonpMapper().objectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    //objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    objectMapper.registerModule(DefaultScalaModule)
    val transport: ElasticsearchTransport = new RestClientTransport(clientJava, new JacksonJsonpMapper(objectMapper))
    val esClient: co.elastic.clients.elasticsearch.ElasticsearchClient = new elasticsearch.ElasticsearchClient(transport)

    esClient
  }

  /*protected def createJavaClient : co.elastic.clients.elasticsearch.ElasticsearchClient = {
    val config: ElasticSearchConfiguration = KoutaConfigurationFactory.configuration.elasticSearchConfiguration;
    lazy val provider2 = {
      val provider = new BasicCredentialsProvider
      val credentials = new UsernamePasswordCredentials(config.username, config.password)
      provider.setCredentials(AuthScope.ANY, credentials)
      provider
    }
    val clientJava = RestClient.builder(
        new HttpHost("pallero-opintopolku.es.eu-west-1.aws.found.io", 9243, "https"))
      .setHttpClientConfigCallback(new HttpClientConfigCallback() {
        def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
          httpClientBuilder.disableAuthCaching
          httpClientBuilder.setDefaultCredentialsProvider(provider2)
        }
      }).build()
    // Create the transport with a Jackson mapper
    val objectMapper = new JacksonJsonpMapper().objectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val transport: ElasticsearchTransport = new RestClientTransport(clientJava, new JacksonJsonpMapper(objectMapper))
    val esClient: co.elastic.clients.elasticsearch.ElasticsearchClient = new elasticsearch.ElasticsearchClient(transport)

    esClient
  }

*/

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
  }
