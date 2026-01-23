package fi.oph.kouta.koutalight

import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.koutalight.database.KoutaDatabaseConnection
import fi.oph.kouta.koutalight.domain.SiirtotiedostoOperation
import fi.oph.kouta.koutalight.service.{KoutaLightSiirtotiedostoService, SiirtotiedostoOperationResults}
import fi.oph.kouta.logging.Logging
import org.json4s.jackson.Serialization.writePretty

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.UUID
import scala.sys.exit
import scala.util.{Failure, Success, Try}

object SiirtotiedostoApp extends Logging with KoutaJsonFormats {
  private val SiirtotiedostoInstantFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of("Europe/Helsinki"))

  private def format(instant: Option[Instant]): String = instant.map(SiirtotiedostoInstantFormat.format).getOrElse("-")

  def main(args: Array[String]): Unit = {
    val opId         = UUID.randomUUID()
    val runStartTime = Instant.now()

    val latestOp = KoutaLightSiirtotiedostoService.findLatestSuccessfulSiirtoOperationData()
    val latestOpWindowEnd = latestOp match {
      case Some(existingOp) => Some(existingOp.windowEnd)
      case None             => None
    }

    Try(KoutaLightSiirtotiedostoService.storeKoulutukset(opId, latestOpWindowEnd, runStartTime)) match {
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
        logger.info(
          s"Operaatio id: ${currentOperation.id.toString}, " +
            s"windowStartTime: ${format(currentOperation.windowStart)}, " +
            s"windowEndTime: ${format(Some(currentOperation.windowEnd))}, " +
            s"runStartTime: ${format(Some(currentOperation.runStart))}, " +
            s"runEndTime: ${format(Some(currentOperation.runEnd))}"
        )

        KoutaLightSiirtotiedostoService.saveSiirtoOperationData(currentOperation)
      case Failure(e) => logger.error(s"Siirtotiedostojen tallentaminen epäonnistui: ${e.toString}")
    }

    KoutaDatabaseConnection.destroy()
    exit
  }
}
