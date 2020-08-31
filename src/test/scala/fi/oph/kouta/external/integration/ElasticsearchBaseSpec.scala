package fi.oph.kouta.external.integration

import fi.oph.kouta.external.TempElasticClient
import fi.oph.kouta.external.elasticsearch.ElasticsearchHealth
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ElasticsearchBaseSpec extends ScalatraFlatSpec {
  "Tests" should "connect to elasticsearch" in {
    new ElasticsearchHealth(TempElasticClient.client).checkStatus().healthy should be(true)
  }
}
