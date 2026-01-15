package fi.oph.kouta.koutalight

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.kouta.logging.Logging

import java.io.File
import scala.util.Try

case class KoutaDatabaseConnectionConfiguration(
    url: String,
    username: String,
    password: String,
    maxConnections: Option[Int],
    minConnections: Option[Int],
    registerMbeans: Option[Boolean]
)

case class S3Configuration(
    transferFileBucket: String,
    transferFileTargetRoleArn: String,
    region: Option[String],
    transferFileSaveRetryCount: Int,
    transferFileMaxItemCount: Int
)

object OvaraKoutaLightConfiguration extends Logging {
  val koutaLightS3Configuration = S3Configuration(
    config.getString("ovara-kouta-light.s3.transferFileBucket"),
    config.getString("ovara-kouta-light.s3.transferFileTargetRoleArn"),
    Try(config.getString("ovara-kouta-light.s3.region")).filter(_.trim.nonEmpty).toOption,
    Try(config.getInt("ovara-kouta-light.s3.transferFileSaveRetryCount")).getOrElse(3),
    Try(config.getInt("ovara-kouta-light.s3.transferFileMaxItemCount")).getOrElse(10000)
  )

  val databaseConfiguration: KoutaDatabaseConnectionConfiguration =
    KoutaDatabaseConnectionConfiguration(
      url = config.getString("ovara-kouta-light.db.url"),
      username = config.getString("ovara-kouta-light.db.user"),
      password = config.getString("ovara-kouta-light.db.password"),
      maxConnections = Option(config.getInt("ovara-kouta-light.db.maxConnections")),
      minConnections = Option(config.getInt("ovara-kouta-light.db.minConnections")),
      registerMbeans = Option(config.getBoolean("ovara-kouta-light.db.registerMbeans"))
    )

  private def createConfig(): Config = {
    var configFile = new File(
      System.getProperty("user.home") +
        "/oph-configuration/ovara-kouta-light.properties"
    )
    if (!configFile.exists()) {
      configFile = new File("ovara-kouta-light.properties")
    }
    ConfigFactory.load(ConfigFactory.parseFile(configFile))
  }

  lazy val config: Config = createConfig()
}
