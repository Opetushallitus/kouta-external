package fi.oph.kouta.koutalight.database

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import fi.oph.kouta.koutalight.OvaraKoutaLightConfiguration
import fi.oph.kouta.logging.Logging
import org.apache.commons.lang3.builder.ToStringBuilder
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.TransactionIsolation
import slick.jdbc.TransactionIsolation.Serializable

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

object KoutaDatabaseConnection extends Logging {
  private val settings = OvaraKoutaLightConfiguration.databaseConfiguration

  logger.warn(settings.username)

  private val db = initDb()

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
    val executor = AsyncExecutor("koutaexternal", maxPoolSize, 1000)

    val className    = classOf[HikariConfig].getSimpleName
    val executorName = ToStringBuilder.reflectionToString(executor)
    val hikariString = ToStringBuilder
      .reflectionToString(hikariConfig)
      .replaceAll("password=.*?,", "password=<HIDDEN>,")

    logger.info(s"Configured Hikari with $className $hikariString and executor $executorName")

    Database.forDataSource(new HikariDataSource(hikariConfig), maxConnections = Some(maxPoolSize), executor)
  }
}
