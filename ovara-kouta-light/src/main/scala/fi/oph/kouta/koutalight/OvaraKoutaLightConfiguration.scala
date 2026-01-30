package fi.oph.kouta.koutalight

import com.typesafe.config.ConfigFactory

import java.io.File
import scala.util.Try

case class KoutaExternalDatabaseConnectionConfiguration(
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

case class OvaraKoutaLightConfiguration(
    s3Configuration: S3Configuration,
    databaseConnectionConfiguration: KoutaExternalDatabaseConnectionConfiguration
)

object Configuration {
  def createConfig(): OvaraKoutaLightConfiguration = {
    var configFile = new File(
      System.getProperty("user.home") +
        "/oph-configuration/ovara-kouta-light.properties"
    )
    if (!configFile.exists()) {
      configFile = new File("ovara-kouta-light.properties")
    }

    val configuration = ConfigFactory.load(ConfigFactory.parseFile(configFile))

    OvaraKoutaLightConfiguration(
      s3Configuration = S3Configuration(
        configuration.getString("ovara-kouta-light.s3.transferFileBucket"),
        configuration.getString("ovara-kouta-light.s3.transferFileTargetRoleArn"),
        Try(configuration.getString("ovara-kouta-light.s3.region")).filter(_.trim.nonEmpty).toOption,
        Try(configuration.getInt("ovara-kouta-light.s3.transferFileSaveRetryCount")).getOrElse(3),
        Try(configuration.getInt("ovara-kouta-light.s3.transferFileMaxItemCount")).getOrElse(10000)
      ),
      databaseConnectionConfiguration = KoutaExternalDatabaseConnectionConfiguration(
        url = configuration.getString("ovara-kouta-light.db.url"),
        username = configuration.getString("ovara-kouta-light.db.user"),
        password = configuration.getString("ovara-kouta-light.db.password"),
        maxConnections = Option(configuration.getInt("ovara-kouta-light.db.maxConnections")),
        minConnections = Option(configuration.getInt("ovara-kouta-light.db.minConnections")),
        registerMbeans = Option(configuration.getBoolean("ovara-kouta-light.db.registerMbeans"))
      )
    )
  }
}
