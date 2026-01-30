package fi.oph.kouta.koutalight

import fi.oph.kouta.koutalight.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.koutalight.domain.SiirtotiedostoOperation
import fi.oph.kouta.koutalight.repository.{KoutaExternalDatabaseConnection, KoutaLightSiirtotiedostoDAO}
import fi.oph.kouta.koutalight.service.{KoutaLightSiirtotiedostoService, SiirtotiedostoOperationResults}
import fi.oph.kouta.koutalight.util.KoutaLightJsonFormats
import fi.oph.kouta.logging.Logging
import org.json4s.jackson.Serialization.writePretty

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.UUID
import scala.sys.exit
import scala.util.{Failure, Success, Try}

object SiirtotiedostoApp extends Logging with KoutaLightJsonFormats {
  val SiirtotiedostoInstantFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of("Europe/Helsinki"))

  def main(args: Array[String]): Unit = {
    val opId         = UUID.randomUUID()
    val runStartTime = Instant.now()

    val configuration = Configuration.createConfig()

    val databaseConnectionConfiguration = configuration.databaseConnectionConfiguration
    val dbConnection                    = KoutaExternalDatabaseConnection(databaseConnectionConfiguration)
    val koutaLightSiirtotiedostoDAO     = new KoutaLightSiirtotiedostoDAO(dbConnection)

    val s3Configuration                       = configuration.s3Configuration
    val koutaLightSiirtotiedostoPalveluClient = new SiirtotiedostoPalveluClient(s3Configuration)

    val koutaLightSiirtotiedostoService =
      new KoutaLightSiirtotiedostoService(koutaLightSiirtotiedostoDAO, koutaLightSiirtotiedostoPalveluClient)

    val latestOp = koutaLightSiirtotiedostoService.findLatestSuccessfulSiirtoOperationData()
    val latestOpWindowEnd = latestOp match {
      case Some(existingOp) => Some(existingOp.windowEnd)
      case None             => None
    }

    Try(koutaLightSiirtotiedostoService.storeKoulutukset(opId, latestOpWindowEnd, runStartTime)) match {
      case Success(response: SiirtotiedostoOperationResults) =>
        val runEndTime = Instant.now()
        val currentOperation = SiirtotiedostoOperation(
          id = opId,
          windowStart = latestOpWindowEnd,
          windowEnd = runStartTime,
          runStart = runStartTime,
          runEnd = runEndTime,
          storedEntitiesCount = response.count
        )
        logger.info(s"Siirtotiedosto-operaatio ajettiin onnistuneesti: ${writePretty(response)}")
        logger.info(s"Operaatioinfo: ${writePretty(currentOperation)}")

        koutaLightSiirtotiedostoService.saveSiirtoOperationData(currentOperation)
      case Failure(e) => logger.error(s"Siirtotiedostojen tallentaminen epäonnistui: ${e.toString}")
    }

    dbConnection.destroy()
    exit
  }
}
