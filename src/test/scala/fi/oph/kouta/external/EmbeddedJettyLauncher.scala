package fi.oph.kouta.external

import fi.oph.kouta.external.elasticsearch.ElasticsearchHealth
import fi.vm.sade.utils.slf4j.Logging

object EmbeddedJettyLauncher extends Logging with KoutaConfigurationConstants {

  val DefaultPort = "8097"

  val TestTemplateFilePath = "src/test/resources/dev-vars.yml"

  def main(args: Array[String]) {
    System.getProperty("kouta-external.embedded", "true") match {
      case x if "false".equalsIgnoreCase(x) => TestSetups.setupWithoutEmbeddedPostgres()
      case _ => TestSetups.setupWithEmbeddedPostgres()
    }

    val port = System.getProperty("kouta-external.port", DefaultPort).toInt

    val elasticStatus = ElasticsearchHealth.checkStatus()
    logger.info(s"Status of Elasticsearch cluster is ${elasticStatus.toString} ${if (elasticStatus.healthy) '\u2714' else '\u274C'}")

    logger.info(s"Starting standalone Kouta-external Jetty on port $port...")
    logger.info(s"http://localhost:$port/kouta-external/swagger")
    new JettyLauncher(port).start().join()
  }

  def setupForTestTemplate() = {
    System.setProperty(SystemPropertyNameConfigProfile, ConfigProfileTemplate)
    System.setProperty(SystemPropertyNameTemplate, TestTemplateFilePath)
  }
}

trait KoutaConfigurationConstants {
  val SystemPropertyNameConfigProfile = "kouta-external.config-profile"
  val SystemPropertyNameTemplate      = "kouta-external.template-file"

  val ConfigProfileDefault  = "default"
  val ConfigProfileTemplate = "template"
}

object TestSetups extends Logging with KoutaConfigurationConstants {

  def setupWithTemplate(port:Int) = {
    logger.info(s"Setting up test template with Postgres port $port")
    Templates.createTestTemplate(port)
    System.setProperty(SystemPropertyNameTemplate, Templates.TestTemplateFilePath)
    System.setProperty(SystemPropertyNameConfigProfile, ConfigProfileTemplate)
  }

  def setupWithEmbeddedPostgres() = {
    logger.info("Starting embedded PostgreSQL!")
    System.getProperty("kouta-external.embeddedPostgresType", "docker") match {
      case x if "host".equalsIgnoreCase(x) => startHostPostgres()
      case _ => startDockerPostgres()
    }
  }

  private def startHostPostgres() = {
    TempDb.start()
    setupWithTemplate(TempDb.port)
  }

  private def startDockerPostgres() = {
    TempDockerDb.start()
    setupWithTemplate(TempDockerDb.port)
  }

  def setupWithoutEmbeddedPostgres()=
    (Option(System.getProperty(SystemPropertyNameConfigProfile)),
      Option(System.getProperty(SystemPropertyNameTemplate))) match {
      case (Some(ConfigProfileTemplate), None) => setupWithDefaultTestTemplateFile()
      case _ => Unit
    }

  def setupWithDefaultTestTemplateFile() = {
    logger.info(s"Using default test template ${Templates.DefaultTemplateFilePath}")
    System.setProperty(SystemPropertyNameTemplate, Templates.TestTemplateFilePath)
    System.setProperty(SystemPropertyNameTemplate, Templates.DefaultTemplateFilePath)
  }

}

object Templates {

  val DefaultTemplateFilePath = "src/test/resources/dev-vars.yml"
  val TestTemplateFilePath = "src/test/resources/embedded-jetty-vars.yml"

  import java.io.{File, PrintWriter}
  import java.nio.file.Files

  import scala.io.Source
  import scala.util.{Failure, Success, Try}

  def createTestTemplate(port:Int, deleteAutomatically:Boolean = false) = {
    Try(new PrintWriter(new File(TestTemplateFilePath))) match {
      case Failure(t) =>
        t.printStackTrace()
        throw t
      case Success(w) => try {
        Source.fromFile(DefaultTemplateFilePath)
          .getLines
          .map {
            case x if x.contains("host_postgresql_koutaexternal_port") => s"host_postgresql_koutaexternal_port: $port"
            case x if x.contains("postgres_app_user") => "postgres_app_user: oph"
            case x if x.contains("host_postgresql_koutaexternal_app_password") => "host_postgresql_koutaexternal_app_password: oph"
            case x if x.contains("host_postgresql_koutaexternal") => "host_postgresql_koutaexternal: localhost"
            case x => x
          }
          .foreach(l => w.println(l))
        w.flush()
      } finally {
        w.close()
      }
        if (deleteAutomatically) {
          Runtime.getRuntime.addShutdownHook(new Thread(() => Templates.deleteTestTemplate()))
        }
    }
  }

  def deleteTestTemplate() = {
    Files.deleteIfExists(new File(TestTemplateFilePath).toPath)
  }
}
