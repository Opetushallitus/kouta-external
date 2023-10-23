package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import co.elastic.clients.elasticsearch._types
import co.elastic.clients.elasticsearch._types.{FieldSort, FieldValue, SortOptions, SortOrder, Time, query_dsl}
import co.elastic.clients.elasticsearch._types.query_dsl.{BoolQuery, MatchQuery, MultiMatchQuery, QueryBuilders, TermQuery, TermsQuery}
import co.elastic.clients.elasticsearch.core.{ClosePointInTimeRequest, OpenPointInTimeRequest, SearchResponse, search}
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference
import scalaz.Scalaz.ToTraverseOps

import java.util.ArrayList
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
//import org.elasticsearch.search.builder.SearchSourceBuilder
//import org.elasticsearch.search.sort.{SortBuilders, SortOrder}
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import fi.oph.kouta.external.domain.indexed.{HakukohdeIndexedTest, HakukohdeJavaClient, MetadataES}
//import org.elasticsearch.action.search
import org.elasticsearch.client
//import org.elasticsearch.index.query.BoolQueryBuilder
//import org.elasticsearch.search.searchafter.SearchAfterBuilder

import java.util
//import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters._
import scala.concurrent.duration.pairIntToDuration


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
//import org.elasticsearch.action.search.{SearchAction, SearchRequestBuilder}
import org.elasticsearch.client
//import org.elasticsearch.index.query.QueryBuilders
//import org.elasticsearch.search.builder.SearchSourceBuilder
//import org.elasticsearch.search.sort.SortBuilders
import fi.oph.kouta.external.{ElasticSearchConfiguration, KoutaConfigurationFactory}
import fi.vm.sade.utils.Timer.timed
import fi.vm.sade.utils.slf4j.Logging
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
//import org.elasticsearch.action
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
import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success}

// OY-4253
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport

trait ResultEntity[T] {
  def toResult: T
}
trait ElasticsearchClient extends Logging {
  val index: String
  val client: ElasticClient
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
        //logger.info("########## Tästä poistettu lokitus!! ##########")
        Future.successful(response.result)
    }
      }

  //protected def createSearchRequestBuilder()

  protected def searchItemsJavaClient[T: ClassTag](queryList: util.List[query_dsl.Query]): List[T] = {
  //protected def searchItemsJavaClient[T: ClassTag](queryList: util.List[query_dsl.Query]): Future[IndexedSeq[T]] = {
    //protected def searchItemsJavaClient[T: ClassTag](queryList: util.List[query_dsl.Query]): Future[List[T]] = {
    logger.info("################# Start searchItemsJavaClient!! #################")
    //logger.info(s"index = " + index)
    //
    //timed(s"SearchItems from ElasticSearch (Query: ${query}", 100) {
  //  timed(s"Search searchItemsJavaClient, queryList = ${queryList}", 100) {
    val searchSize =5000
    val esClient: co.elastic.clients.elasticsearch.ElasticsearchClient = createJavaClient
    val openPitRequest = new OpenPointInTimeRequest.Builder()
      .index(index).keepAlive(new Time.Builder().time("1m").build()).build()
    val openPointInTimeResponse = esClient.openPointInTime(openPitRequest)
    val pitr: PointInTimeReference = new PointInTimeReference.Builder()
      .keepAlive(new Time.Builder().time("1m").build()).id(openPointInTimeResponse.id()).build()
    val sortOpt = new SortOptions.Builder().field(FieldSort.of(f => f.field("oid.keyword").order(SortOrder.Asc))).build()
    val query = QueryBuilders.bool.must(queryList).build._toQuery()
    var srBuilder = new SearchRequest.Builder()
      .query(query).size(searchSize).sort(sortOpt).pit(pitr)

    val searchRequest = srBuilder.build()

    try {


      //QueryBuilders.bool.must(queryList).build._toQuery()
      // TODO tarkista voiko em. mapperia käyttää createJavaClient:ssa (ObjectMapper vs. JsonpMapper)

      var response: co.elastic.clients.elasticsearch.core.SearchResponse[Object] = esClient.search(searchRequest,classOf[Object])
      var hitList2 =  response.hits().hits()
      logger.info("searchRequest = " + searchRequest)
      //var hitList: List[search.Hit[Object]] = response.hits().hits()
      var hitList = new ArrayList[search.Hit[Object]]
      hitList.addAll(response.hits().hits())

      if(!hitList.isEmpty){
        val ekaosuma  = hitList.get(0).source()
        logger.info("histListin ekaosuma = " + ekaosuma)
      }
      var hitCount = hitList.size()
      logger.info("hitList.size() = " + hitList.size())
      var hitCountTotal = 0
      hitCountTotal += hitCount

/*
     // Search rest of results (While hitCount equals searchSize there is more search results)
      while(hitCount == searchSize ) {
        val lastHit = response.hits().hits().last
        val lastHitSort = lastHit.sort()

        srBuilder = new SearchRequest.Builder().query(query).sort(sortOpt).size(searchSize).pit(pitr).searchAfter(lastHitSort)
        response = esClient.search(srBuilder.build(), classOf[Object])
        hitList.addAll(response.hits().hits())
        hitCount = response.hits().hits().size()
        hitCountTotal += hitCount
        logger.info("hitCountTotal = " + hitCountTotal)
      }*/


      logger.info("hitCountTotal = " + hitCountTotal)
      //Future {response.hits().hits().map(hit => mapper.convertValue(hit, classTag[T].runtimeClass).asInstanceOf[T]).toList}
      //Future{
        //hitList.map(hit => mapper.convertValue(hit, classTag[T].runtimeClass).asInstanceOf[T]).toList
      val listToReturn = hitList.map(hit => mapper.convertValue(hit.source(), classTag[T].runtimeClass).asInstanceOf[T]).toList
      listToReturn
        //hitList.map(hit => mapper.convertValue(hit, classTag[T].runtimeClass).asInstanceOf[T]).toIndexedSeq
      //}
      } catch {
        case e: JsonParsingException => println("Virhe: " + e.printStackTrace())
          //Future.apply(List.empty)
       // Future.apply(List.empty.toIndexedSeq)
        List.empty
      }
  //  }


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
    logger.info("################# Start searchItems!! #################")
    //logger.info(s"queryOldSearch = " + query)

    timed(s"SearchItems from ElasticSearch (Query: ${query}", 100) {
      implicit val duration: FiniteDuration = Duration(1, TimeUnit.MINUTES)
   //   val seq = SearchIterator.iterate[T](client, search(index).keepAlive("1m").size(5)).toIndexedSeq
   //   logger.info("sqe = " + seq)
      query.fold[Future[IndexedSeq[T]]]({
        Future(
          SearchIterator.iterate[T](client, ElasticApi.search(index).keepAlive("1m").size(500)).toIndexedSeq
        )
      })(q => {
        val request = ElasticApi.search(index).query(q).keepAlive("1m").size(5)
        logger.info(s"XXXX Elasticsearch request: ${request.show}")
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
    //val objectMapper: ObjectMapper = new JacksonJsonpMapper().objectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    //objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    //objectMapper.registerModule(DefaultScalaModule)
    //val transport: ElasticsearchTransport = new RestClientTransport(clientJava, new JacksonJsonpMapper(objectMapper))
    val transport: ElasticsearchTransport = new RestClientTransport(clientJava, new JacksonJsonpMapper(mapper))
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
