package fi.oph.kouta.external.integration

import fi.oph.kouta.external.TempElasticDockerClient
import fi.oph.kouta.external.elasticsearch.ElasticsearchHealth
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ElasticsearchBaseSpec extends ScalatraFlatSpec {
  "Tests" should "connect to elasticsearch" in {
    new ElasticsearchHealth(TempElasticDockerClient.client).checkStatus().healthy should be(true)
  }
}
