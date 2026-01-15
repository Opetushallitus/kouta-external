package fi.oph.kouta.koutalight

import fi.oph.kouta.koutalight.database.KoutaDatabaseConnection
import fi.oph.kouta.koutalight.repository.SiirtotiedostoOperation
import fi.oph.kouta.koutalight.service.KoutaLightSiirtotiedostoService
import fi.oph.kouta.logging.Logging

import java.time.LocalDateTime
import java.util.UUID
import scala.sys.exit
import scala.util.{Failure, Success, Try}

object SiirtotiedostoApp extends Logging {
  def main(args: Array[String]): Unit = {
    val opId         = UUID.randomUUID()
    val runStartTime = LocalDateTime.now()

    val latestOp = KoutaLightSiirtotiedostoService.findLatestSuccessfulSiirtoOperationData()
    val latestOpWindowEnd = latestOp match {
      case Some(existingOp) => Some(existingOp.windowEnd)
      case None             => None
    }

    Try(KoutaLightSiirtotiedostoService.storeKoulutukset(opId, latestOpWindowEnd)) match {
      case Success(response: String) =>
        logger.info(response)
        val runEndTime = LocalDateTime.now()
        val currentOperation = SiirtotiedostoOperation(
          id = opId,
          windowStart = latestOpWindowEnd,
          windowEnd = LocalDateTime.now(),
          runStart = runStartTime,
          runEnd = runEndTime
        )

        KoutaLightSiirtotiedostoService.saveSiirtoOperationData(currentOperation)
      case Failure(e) => logger.error(e.toString)
    }

    KoutaDatabaseConnection.destroy()
    exit
  }
}
