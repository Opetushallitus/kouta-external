package fi.oph.kouta.koutalight.service

import fi.oph.kouta.koutalight.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.koutalight.repository.{KoutaLightSiirtotiedostoDAO, SiirtotiedostoOperation}

import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable.ListBuffer

case class SiirtotiedostoOperationResults(keys: Seq[String], count: Int)

object KoutaLightSiirtotiedostoService
    extends KoutaLightSiirtotiedostoService(KoutaLightSiirtotiedostoDAO, SiirtotiedostoPalveluClient)

class KoutaLightSiirtotiedostoService(
    koutaLightSiirtotiedostoDAO: KoutaLightSiirtotiedostoDAO,
    siirtotiedostoPalveluClient: SiirtotiedostoPalveluClient
) {

  def storeKoulutukset(
      operationId: UUID,
      operationWindowStartTime: Option[LocalDateTime],
      operationWindowEndTime: LocalDateTime
  ): SiirtotiedostoOperationResults = {
    var koulutukset =
      koutaLightSiirtotiedostoDAO.getKoulutukset(operationWindowStartTime, operationWindowEndTime, None)
    var operationSubId              = 0
    val keyList: ListBuffer[String] = ListBuffer()
    var count                       = koulutukset.length

    while (koulutukset.nonEmpty) {
      operationSubId += 1
      keyList += siirtotiedostoPalveluClient.saveSiirtotiedosto(
        contentType = "koulutus",
        content = koulutukset,
        operationId = operationId,
        operationSubId = operationSubId
      )

      val lastKoulutusId = koulutukset.last.id
      koulutukset = koutaLightSiirtotiedostoDAO.getKoulutukset(
        operationWindowStartTime,
        operationWindowEndTime,
        lastKoulutusId
      )

      count += koulutukset.length
    }

    SiirtotiedostoOperationResults(keyList, count)
  }

  def findLatestSuccessfulSiirtoOperationData(): Option[SiirtotiedostoOperation] = {
    koutaLightSiirtotiedostoDAO.getLatestSiirtotiedostoOperationData
  }

  def saveSiirtoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): Int = {
    koutaLightSiirtotiedostoDAO.saveSiirtotiedostoOperationData(siirtotiedostoOperation)
  }
}
