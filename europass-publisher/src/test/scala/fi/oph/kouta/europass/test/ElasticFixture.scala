package fi.oph.kouta.europass.test

import org.scalatest.{BeforeAndAfterAll, Suite}
import scala.sys.process.Process

trait ElasticFixture extends BeforeAndAfterAll { this: Suite =>

  private def useFixture(): Boolean =
    !(System.getenv("TEST_USE_PRERUN_ELASTIC") == "true")

  override def beforeAll() {
    if (useFixture()) {
      Process("docker-compose up -d kouta-elastic europass-s3").!
      Process("docker-compose up elasticdump-loader s3-configurator").!
    }
  }

  override def afterAll() {
    if (useFixture()) {
      Process("docker-compose down").!
    }
  }

}
