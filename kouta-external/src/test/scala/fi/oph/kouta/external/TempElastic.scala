package fi.oph.kouta.external

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import fi.oph.kouta.external.TempElasticClient.createJavaClient
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.utility.DockerImageName

object TempElasticClient {
  val useTestContainersElastic = true // Aseta falseksi, jos haluat ajaa testejä jo käynnissä olevaa elasticsearchia vasten
  var url = ""
  if (useTestContainersElastic) {
    url = s"http://localhost:${TempElastic.start()}"
  } else {
    url = KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl
  }

  val client = ElasticClient(JavaClient(ElasticProperties(url)))
  val clientJava = createJavaClient(url)

  def createJavaClient(elasticUrl: String): co.elastic.clients.elasticsearch.ElasticsearchClient = {
    //val config: ElasticSearchConfiguration = KoutaConfigurationFactory.configuration.elasticSearchConfiguration;
    val clientJava = RestClient.builder(HttpHost.create(elasticUrl)).build()

    val mapper: ObjectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .registerModule(DefaultScalaModule)

    val transport: ElasticsearchTransport = new RestClientTransport(clientJava, new JacksonJsonpMapper(mapper))
    val esClient: co.elastic.clients.elasticsearch.ElasticsearchClient = new ElasticsearchClient(transport)
    esClient
  }
}

private object TempElastic {
  var elastic: Option[ElasticsearchContainer] = None

  def start(): Int = {
    try {
      get().getMappedPort(9200)
    } finally {
      Runtime.getRuntime.addShutdownHook(new Thread(() => stop()))
    }
  }

  def create(): ElasticsearchContainer = {
    val dockerImager = DockerImageName.parse("190073735177.dkr.ecr.eu-west-1.amazonaws.com/utility/elasticsearch-kouta:8.5.2").asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
    val embeddedElastic = new ElasticsearchContainer(dockerImager)
    embeddedElastic.getEnvMap.put("xpack.security.enabled", "false")
    embeddedElastic.getEnvMap.put("ES_JAVA_OPTS", "-Xms2g -Xmx2g")

    embeddedElastic.start()
    elastic = Some(embeddedElastic)
    embeddedElastic
  }

  def stop(): Unit = {
    elastic.foreach(_.stop())
    elastic.foreach(_.close())
    elastic = None
  }

  def get(): ElasticsearchContainer = elastic.getOrElse(create())
}
