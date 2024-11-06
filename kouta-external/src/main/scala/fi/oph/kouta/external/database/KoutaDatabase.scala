package fi.oph.kouta.external.database

import java.util.concurrent.TimeUnit

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import fi.oph.kouta.external.{KoutaConfigurationFactory, KoutaDatabaseConfiguration}
import fi.oph.kouta.logging.Logging
import org.apache.commons.lang3.builder.ToStringBuilder
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.Configuration
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.TransactionIsolation
import slick.jdbc.TransactionIsolation.Serializable

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try


object KoutaDatabase extends Logging {

  val settings: KoutaDatabaseConfiguration = KoutaConfigurationFactory.configuration.databaseConfiguration

  logger.warn(settings.username)

  migrate()

  val db = initDb()

  def init(): Unit = {}

  def runBlocking[R](operations: DBIO[R], timeout: Duration = Duration(10, TimeUnit.MINUTES)): R = {
    Await.result(
      db.run(operations.withStatementParameters(statementInit = st => st.setQueryTimeout(timeout.toSeconds.toInt))),
      timeout + Duration(1, TimeUnit.SECONDS)
    )
  }

  def runBlockingTransactionally[R](operations: DBIO[R], timeout: Duration = Duration(20, TimeUnit.SECONDS), isolation: TransactionIsolation = Serializable): Try[R] = {
    Try(runBlocking(operations.transactionally.withTransactionIsolation(isolation), timeout))
  }

  def destroy(): Unit = {
    db.close()
  }

  private def initDb() = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(settings.url)
    hikariConfig.setUsername(settings.username)
    hikariConfig.setPassword(settings.password)
    val maxPoolSize = settings.maxConnections.getOrElse(10)
    hikariConfig.setMaximumPoolSize(maxPoolSize)
    settings.minConnections.foreach(hikariConfig.setMinimumIdle)
    settings.registerMbeans.foreach(hikariConfig.setRegisterMbeans)
    //settings.initializationFailTimeout.foreach(hikariConfig.setI)
    //hikariConfig.setLeakDetectionThreshold(settings.leakDetectionThresholdMillis.getOrElse(settings.getMaxLifetime))
    val executor = AsyncExecutor("koutaexternal", maxPoolSize, 1000)

    val className    = classOf[HikariConfig].getSimpleName
    val executorName = ToStringBuilder.reflectionToString(executor)
    val hikariString = ToStringBuilder
      .reflectionToString(hikariConfig)
      .replaceAll("password=.*?,", "password=<HIDDEN>,")

    logger.info(s"Configured Hikari with $className $hikariString and executor $executorName")

    Database.forDataSource(new HikariDataSource(hikariConfig), maxConnections = Some(maxPoolSize), executor)
  }

  private def migrate(): Unit = {
    val flyway = Flyway.configure.locations("flyway/migration").dataSource(settings.url, settings.username, settings.password).load
    flyway.migrate
  }
}
