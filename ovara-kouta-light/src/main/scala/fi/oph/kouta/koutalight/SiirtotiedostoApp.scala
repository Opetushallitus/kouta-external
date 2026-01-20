package fi.oph.kouta.koutalight

import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.koutalight.database.KoutaDatabaseConnection
import fi.oph.kouta.koutalight.repository.SiirtotiedostoOperation
import fi.oph.kouta.koutalight.service.{KoutaLightSiirtotiedostoService, SiirtotiedostoOperationResults}
import fi.oph.kouta.logging.Logging
import org.json4s.jackson.Serialization.writePretty

import java.time.LocalDateTime
import java.util.UUID
import scala.sys.exit
import scala.util.{Failure, Success, Try}

object SiirtotiedostoApp extends Logging with KoutaJsonFormats {
  def main(args: Array[String]): Unit = {
    val opId         = UUID.randomUUID()
    val runStartTime = LocalDateTime.now()

    val latestOp = KoutaLightSiirtotiedostoService.findLatestSuccessfulSiirtoOperationData()
    val latestOpWindowEnd = latestOp match {
      case Some(existingOp) => Some(existingOp.windowEnd)
      case None             => None
    }

    Try(KoutaLightSiirtotiedostoService.storeKoulutukset(opId, latestOpWindowEnd, runStartTime)) match {
      case Success(response: SiirtotiedostoOperationResults) =>
        logger.info(response.toString)
        val runEndTime = LocalDateTime.now()
        val currentOperation = SiirtotiedostoOperation(
          id = opId,
          windowStart = latestOpWindowEnd,
          windowEnd = LocalDateTime.now(),
          runStart = runStartTime,
          runEnd = runEndTime
        )
        logger.info("Siirtiedostojen tallentaminen onnistui {}", writePretty(currentOperation))

        KoutaLightSiirtotiedostoService.saveSiirtoOperationData(currentOperation)
      case Failure(e) => logger.error(s"Siirtiedostojen tallentaminen epäonnistui: ${e.toString}")
    }

    KoutaDatabaseConnection.destroy()
    exit
  }
}
