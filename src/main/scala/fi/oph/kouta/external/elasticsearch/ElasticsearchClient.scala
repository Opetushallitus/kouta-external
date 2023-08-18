package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch

import java.util
import co.elastic.clients.elasticsearch.core.SearchRequest
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
  protected def searchItemsSearchAfter[T: HitReader : ClassTag](query: Option[Query]): Future[IndexedSeq[T]] = {
    logger.info("Suoritetaan searchItemsSearchAfter!")
    logger.info(s"query = " + query.get)
    logger.info(s"index = " + index)

    val newclient = createJavaClient
  //  val builder = new GetRequest.Builder
  //  builder.index(index)
  //  builder.id("1.2.246.562.20.00000000000000013343") // This should be fixed to real id

    /* TÄMÄ TOIMII, MUTTA EI SEARCH AFTER!!
    val srBuilder = new SearchRequest.Builder()
    srBuilder.index(index)
    val searchRequest = srBuilder.build()
    */
    val srBuilder = new SearchRequest.Builder()
    srBuilder.index(index)
    val searchRequest = srBuilder.build()
    val list = searchRequest.searchAfter()

    //val searchRequest = new SearchRequest(index);
    //val searchSourceBuilder = new SearchSourceBuilder()
    val arrSearchAfter: util.List[HakukohdeIndexed] = new util.ArrayList[HakukohdeIndexed]();
    //searchSourceBuilder.searchAfter(Array[Object](4))
    // searchSourceBuilder.searchAfter(new Object[]{sortAfterValue});

    //List[HakukohdeIndexed] list = searchRequest.searchAfter()

//    val aa = new HakukohdeIndexed()


    try {
      //val response: co.elastic.clients.elasticsearch.core.GetResponse[HakukohdeIndexed] = esClient.get(getRequest,classOf[HakukohdeIndexed])
      val response: co.elastic.clients.elasticsearch.core.SearchResponse[HakukohdeIndexed] = newclient.search(searchRequest,classOf[HakukohdeIndexed])

      logger.info("response GetResponse = " + response.toString)
    } catch {
      case e: JsonParsingException => println("Virhe: " + e.printStackTrace())
      case e: Exception => "Tämä virhe: " + e.printStackTrace()
    }

    /*

        //SECOND REQUEST WITH SEARCH AFTER
        builder = new SearchSourceBuilder();
        builder.sort(SortBuilders.fieldSort("name").order(SortOrder.DESC));
        builder.size(2);
        builder.query(QueryBuilders.matchAllQuery());

        searchRequest = new SearchRequest();
        searchRequest.indices("idx_movies_suggest");
        searchRequest.source(builder);

        //USING SEARCH AFTER
        Object[] arrSearchAfter = new Object[]{"batman"};
        builder.searchAfter(arrSearchAfter);

        searchRequest = new SearchRequest();
        searchRequest.indices("idx_movies_suggest");
        searchRequest.source(builder);
        response = client.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(response);
     */

    /*
    val search = esClient.search((s) => s.index("products").query((q) => q.term((t) => t.field("name").value((v) => v.stringValue("bicycle")))), classOf[Nothing])

    import scala.collection.JavaConversions._
    for (hit <- search.hits.hits) {
      processProduct(hit.source)
    }
    */

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
  }


  protected def searchItems[T: HitReader: ClassTag](query: Option[Query]): Future[IndexedSeq[T]] = {
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
    if(debugJsonEnabled) logger.info(s"Elastic search response: ${response.sourceAsString}")
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
    val transport: ElasticsearchTransport = new RestClientTransport(clientJava, new JacksonJsonpMapper())
    val esClient: co.elastic.clients.elasticsearch.ElasticsearchClient = new elasticsearch.ElasticsearchClient(transport)

    esClient
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
  }
