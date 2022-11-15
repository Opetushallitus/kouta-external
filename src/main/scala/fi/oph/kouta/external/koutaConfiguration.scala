package fi.oph.kouta.external

import com.typesafe.config.{Config => TypesafeConfig}
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.vm.sade.properties.OphProperties
import fi.oph.kouta.util.{KoutaBaseConfig, KoutaConfigFactory}
import scala.util.Try

case class KoutaDatabaseConfiguration(
    url: String,
    port: Int,
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
    extends KoutaBaseConfig(config, urlProperties) {

  val hakukohderyhmaConfiguration: HakukohderyhmaConfiguration =
    HakukohderyhmaConfiguration(cacheTtlMinutes =
      config.getLong("kouta-external.hakukohderyhma.cacheTtlMinutes"))

  val databaseConfiguration: KoutaDatabaseConfiguration =
    KoutaDatabaseConfiguration(
      url = config.getString("kouta-external.db.url"),
      port = config.getInt("kouta-external.db.port"),
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

object KoutaConfigurationFactory extends KoutaConfigFactory[KoutaConfiguration]("kouta-external") {
  def createConfigCaseClass(config: TypesafeConfig, urlProperties: OphProperties) = KoutaConfiguration(config, urlProperties)
}
