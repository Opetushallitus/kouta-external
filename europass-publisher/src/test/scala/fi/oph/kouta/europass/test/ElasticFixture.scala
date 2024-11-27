package fi.oph.kouta.europass.test

import org.scalatest.{BeforeAndAfterAll, Suite}
import scala.sys.process.Process

trait ElasticFixture extends BeforeAndAfterAll { this: Suite =>

  override def beforeAll() {
    if (System.getenv("TEST_USE_PRERUN_ELASTIC") == null) {
      Process("docker-compose up -d kouta-elastic europass-s3").!
      Process("docker-compose up elasticdump-loader s3-configurator").!
    }
  }

  override def afterAll() {
    if (System.getenv("TEST_USE_PRERUN_ELASTIC") == null) {
      Process("docker-compose down").!
    }
  }

}
