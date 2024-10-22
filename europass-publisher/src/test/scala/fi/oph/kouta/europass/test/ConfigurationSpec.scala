package fi.oph.kouta.europass.test

import fi.oph.kouta.europass.EuropassConfiguration
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ConfigurationSpec extends ScalatraFlatSpec {
  "configuration" should "have Elasticsearch details" in {
    assert(EuropassConfiguration.config.getString("europass-publisher.elasticsearch.url").startsWith("http"))
  }
}
