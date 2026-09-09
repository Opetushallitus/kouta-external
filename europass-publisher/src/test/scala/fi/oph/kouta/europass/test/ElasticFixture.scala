package fi.oph.kouta.europass.test

import fi.oph.kouta.europass.ElasticClient
import org.json4s.DefaultFormats
import org.scalatest.{BeforeAndAfterAll, Suite}

import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import scala.sys.process.Process
import scala.util.Try

trait ElasticFixture extends BeforeAndAfterAll { this: Suite =>
  private implicit val formats = DefaultFormats

  // relative to this module's basedir, which is the forked test JVM's working directory
  private val dumpDir = new File("../kouta-external/src/test/resources/elastic_dump")
  private val dumpHashIndex = "test-fixture-meta"
  private val dumpHashDocId = "elastic-dump-hash"

  private def useFixture(): Boolean =
    !(System.getenv("TEST_USE_PRERUN_ELASTIC") == "true")

  // Elasticsearch's health API blocks server-side until shards are allocated,
  // so waiting on it here avoids racing document reads against shard
  // recovery right after a container (re)start.
  private def clusterHealthy(): Boolean =
    Try((ElasticClient.getJson("_cluster/health?wait_for_status=yellow&timeout=30s") \ "status")
      .extract[String]).toOption.exists(status => status == "yellow" || status == "green")

  // docker-compose up -d does not wait for the container to accept
  // connections at all, so retry until elasticsearch is reachable.
  private def waitForElastic(retriesLeft: Int = 30): Boolean =
    if (clusterHealthy()) {
      true
    } else if (retriesLeft > 0) {
      Thread.sleep(1000)
      waitForElastic(retriesLeft - 1)
    } else {
      false
    }

  private def elasticDocCount(): Int =
    Try((ElasticClient.getJson("_all/_count") \ "count").extract[Int]).getOrElse(0)

  // Fingerprints the dump files' names and contents, so an edit to them is detected
  // even though elasticsearch already has (now stale) data loaded from an earlier run.
  private def currentDumpHash(): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    Option(dumpDir.listFiles()).getOrElse(Array.empty).sortBy(_.getName).foreach { file =>
      digest.update(file.getName.getBytes("UTF-8"))
      digest.update(Files.readAllBytes(file.toPath))
    }
    digest.digest().map("%02x".format(_)).mkString
  }

  private def storedDumpHash(): Option[String] =
    Try((ElasticClient.getJson(s"$dumpHashIndex/_doc/$dumpHashDocId") \ "_source" \ "hash").extract[String]).toOption

  private def storeDumpHash(hash: String): Unit =
    ElasticClient.postJson(s"$dumpHashIndex/_doc/$dumpHashDocId", Map("hash" -> hash))

  override def beforeAll() {
    if (useFixture()) {
      Process("docker-compose up -d kouta-elastic europass-s3").!
      println("Waiting for elasticsearch to be ready...")
      val dumpUpToDate = waitForElastic() && elasticDocCount() > 0 && storedDumpHash().contains(currentDumpHash())
      if (dumpUpToDate) {
        println("Elasticsearch already has up-to-date dump data, skipping import.")
      } else {
        Process("docker-compose up elasticdump-loader s3-configurator").!
        Try(storeDumpHash(currentDumpHash()))
      }
    }
  }

  override def afterAll() {
    if (useFixture()) {
      Process("docker-compose down").!
    }
  }

}
