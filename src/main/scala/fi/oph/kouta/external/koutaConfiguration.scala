package fi.oph.kouta.external

import com.typesafe.config.{Config => TypesafeConfig}
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.utils.config.{ApplicationSettings, ApplicationSettingsLoader, ApplicationSettingsParser, ConfigTemplateProcessor}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.duration.DurationInt
import scala.util.Try

case class KoutaDatabaseConfiguration(
    url: String,
    username: String,
    password: String,
    numThreads: Option[Int],
    maxConnections: Option[Int],
    minConnections: Option[Int],
    registerMbeans: Option[Boolean],
    initializationFailTimeout: Option[Int],
    leakDetectionThresholdMillis: Option[Int]
)

case class SecurityConfiguration(
    casUrl: String,
    casServiceIdentifier: String,
    kayttooikeusUrl: String,
    rootOrganisaatio: OrganisaatioOid,
    externalApiModifyEnabled: Boolean
)

case class ElasticSearchConfiguration(
    elasticUrl: String,
    authEnabled: Boolean,
    username: String,
    password: String
)

case class CasClientConfiguration(username: String, password: String)

case class HakukohderyhmaConfiguration(cacheTtlMinutes: Long)

case class KoutaConfiguration(config: TypesafeConfig, urlProperties: OphProperties)
    extends ApplicationSettings(config) {

  val hakukohderyhmaConfiguration: HakukohderyhmaConfiguration =
    HakukohderyhmaConfiguration(cacheTtlMinutes =
      config.getLong("kouta-external.hakukohderyhma.cacheTtlMinutes"))

  val databaseConfiguration: KoutaDatabaseConfiguration =
    KoutaDatabaseConfiguration(
      url = config.getString("kouta-external.db.url"),
      username = config.getString("kouta-external.db.user"),
      password = config.getString("kouta-external.db.password"),
      numThreads = Option(config.getInt("kouta-external.db.numThreads")),
      maxConnections = Option(config.getInt("kouta-external.db.maxConnections")),
      minConnections = Option(config.getInt("kouta-external.db.minConnections")),
      registerMbeans = Option(config.getBoolean("kouta-external.db.registerMbeans")),
      initializationFailTimeout = Option(config.getInt("kouta-external.db.initializationFailTimeout")),
      leakDetectionThresholdMillis = Option(config.getInt("kouta-external.db.leakDetectionThresholdMillis"))
    )

  val securityConfiguration = SecurityConfiguration(
    casUrl = config.getString("cas.url"),
    casServiceIdentifier = config.getString("kouta-external.cas.service"),
    kayttooikeusUrl = config.getString("kayttooikeus-service.userDetails.byUsername"),
    rootOrganisaatio = OrganisaatioOid("1.2.246.562.10.00000000001"),
    Try(config.getBoolean("kouta.external-api.modify.enabled")).getOrElse(false)
  )

  val elasticSearchConfiguration = ElasticSearchConfiguration(
    config.getString("kouta-external.elasticsearch.url"),
    config.getBoolean("kouta-external.elasticsearch.auth-enabled"),
    config.getString("kouta-external.elasticsearch.username"),
    config.getString("kouta-external.elasticsearch.password")
  )

  val clientConfiguration = CasClientConfiguration(
    username = config.getString("kouta-external.cas.username"),
    password = config.getString("kouta-external.cas.password")
  )
}

trait KoutaConfigurationConstants {
  val SystemPropertyNameConfigProfile = "kouta-external.config-profile"
  val SystemPropertyNameTemplate      = "kouta-external.template-file"

  val ConfigProfileDefault  = "default"
  val ConfigProfileTemplate = "template"
}

object KoutaConfigurationFactory extends Logging with KoutaConfigurationConstants {

  val profile: String = System.getProperty(SystemPropertyNameConfigProfile, ConfigProfileDefault)
  logger.info(s"Using profile '$profile'")

  val configuration: KoutaConfiguration = profile match {
    case ConfigProfileDefault  => loadOphConfiguration()
    case ConfigProfileTemplate => loadTemplatedConfiguration()
    case _ =>
      throw new IllegalArgumentException(
        s"Unknown profile '$profile'! Cannot load oph-properties! Use either " +
          s"'$ConfigProfileDefault' or '$ConfigProfileTemplate' profiles."
      )
  }

  def init(): Unit = {}

  private def loadOphConfiguration(): KoutaConfiguration = {
    val configFilePath = System.getProperty("user.home") + "/oph-configuration/kouta-external.properties"

    val applicationSettingsParser = new ApplicationSettingsParser[KoutaConfiguration] {
      override def parse(config: TypesafeConfig): KoutaConfiguration =
        KoutaConfiguration(config, new OphProperties(configFilePath))
    }

    logger.info(s"Reading properties from '$configFilePath'")
    ApplicationSettingsLoader.loadSettings(configFilePath)(applicationSettingsParser)
  }

  private def loadTemplatedConfiguration(overrideFromSystemProperties: Boolean = false): KoutaConfiguration = {
    val templateFilePath = Option(System.getProperty(SystemPropertyNameTemplate)).getOrElse(
      throw new IllegalArgumentException(
        s"Using 'template' profile but '${SystemPropertyNameTemplate}' " +
          "system property is missing. Cannot create oph-properties!"
      )
    )

    implicit val applicationSettingsParser = new ApplicationSettingsParser[KoutaConfiguration] {
      override def parse(c: TypesafeConfig): KoutaConfiguration =
        KoutaConfiguration(c, new OphProperties("src/test/resources/kouta-external.properties") {
          addDefault("host.virkailija", c.getString("host.virkailija"))
        })
    }

    logger.info(s"Reading template variables from '${templateFilePath}'")
    ConfigTemplateProcessor.createSettings("kouta-external", templateFilePath)
  }
}
