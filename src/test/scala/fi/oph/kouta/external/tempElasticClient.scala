package fi.oph.kouta.external

import java.io.File
import java.util.concurrent.TimeUnit

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import fi.vm.sade.utils.tcp.ChooseFreePort
import pl.allegro.tech.embeddedelasticsearch.{EmbeddedElastic, PopularProperties}

object TempElasticClient {
  lazy val url = s"http://localhost:${TempElastic.start()}"
  lazy val client: ElasticClient = ElasticClient(ElasticProperties(url))
}

object TempElastic {
  var elasticInstance: Option[EmbeddedElastic] = None

  private val port            = new ChooseFreePort().chosenPort
  private val timeoutInMillis = 60 * 1000

  def start(): Int = get().getHttpPort

  def create(): EmbeddedElastic = {
    val embeddedElastic = EmbeddedElastic
      .builder()
      .withElasticVersion("6.7.2")
      .withInstallationDirectory(new File("target/embeddedElasticsearch"))
      .withSetting(PopularProperties.HTTP_PORT, port)
      .withSetting("path.repo", "embeddedElasticsearch")
      .withSetting(PopularProperties.CLUSTER_NAME, "elasticsearch")
      .withSetting("discovery.zen.ping.unicast.hosts", s"127.0.0.1:$port")
      .withStartTimeout(timeoutInMillis, TimeUnit.MILLISECONDS)
      .build

    elasticInstance = Some(embeddedElastic.start())
    elasticInstance.get
  }

  def stop(): Unit = {
    elasticInstance.foreach(_.stop())
    elasticInstance = None
  }

  def get(): EmbeddedElastic = elasticInstance.getOrElse(create())
}
