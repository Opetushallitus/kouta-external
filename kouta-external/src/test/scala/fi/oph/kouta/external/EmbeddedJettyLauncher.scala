package fi.oph.kouta.external

import fi.oph.kouta.external.elasticsearch.ElasticsearchHealth
import fi.oph.kouta.logging.Logging

object EmbeddedJettyLauncher extends Logging {

  val DefaultPort = "8097"

  def main(args: Array[String]): Unit = {
    KoutaConfigurationFactory.setupWithDevTemplate();
    TestSetups.setupPostgres()

    val port = System.getProperty("kouta-external.port", DefaultPort).toInt

    val elasticStatus = ElasticsearchHealth.checkStatus()
    logger.info(
      s"Status of Elasticsearch cluster is ${elasticStatus.toString} ${if (elasticStatus.healthy) '\u2714' else '\u274C'}"
    )

    logger.info(s"Starting standalone Kouta-external Jetty on port $port...")
    logger.info(s"http://localhost:$port/kouta-external/swagger")
    new JettyLauncher(port).start().join()
  }
}

object TestSetups extends Logging {
  def setupPostgres() = {
    System.getProperty("kouta-external.embedded", "true") match {
      case x if "true".equalsIgnoreCase(x) => TempDockerDb.start()
      case _ =>
    }
  }
}
