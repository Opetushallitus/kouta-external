package fi.oph.kouta.external

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import org.testcontainers.elasticsearch.ElasticsearchContainer

object TempElasticClient {
  val url    = s"http://localhost:${TempElastic.start()}"
  val client = ElasticClient(JavaClient(ElasticProperties(url)))
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
    val embeddedElastic = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.10.2")

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
