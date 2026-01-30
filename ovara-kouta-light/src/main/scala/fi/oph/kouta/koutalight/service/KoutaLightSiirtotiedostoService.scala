package fi.oph.kouta.koutalight.service

import fi.oph.kouta.koutalight.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.koutalight.domain.SiirtotiedostoOperation
import fi.oph.kouta.koutalight.repository.KoutaLightSiirtotiedostoDAO

import java.time.Instant
import java.util.UUID
import scala.collection.mutable.ListBuffer

case class SiirtotiedostoOperationResults(s3ObjectKeys: Seq[String], storedKoulutusIds: Seq[UUID], count: Int)

class KoutaLightSiirtotiedostoService(
    koutaLightSiirtotiedostoDAO: KoutaLightSiirtotiedostoDAO,
    siirtotiedostoPalveluClient: SiirtotiedostoPalveluClient
) {

  def storeKoulutukset(
      operationId: UUID,
      operationWindowStartTime: Option[Instant],
      operationWindowEndTime: Instant
  ): SiirtotiedostoOperationResults = {
    val maxNumberOfItemsInFile = siirtotiedostoPalveluClient.config.transferFileMaxItemCount
    var koulutukset =
      koutaLightSiirtotiedostoDAO.getKoulutukset(
        operationWindowStartTime,
        operationWindowEndTime,
        None,
        maxNumberOfItemsInFile
      )

    var operationSubId              = 0
    val keyList: ListBuffer[String] = ListBuffer()
    var koulutusIds                 = koulutukset.flatMap(_.id)

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
        lastKoulutusId,
        maxNumberOfItemsInFile
      )

      koulutusIds = koulutusIds ++ koulutukset.flatMap(_.id)
    }

    val count = koulutusIds.length
    SiirtotiedostoOperationResults(keyList, koulutusIds, count)
  }

  def findLatestSuccessfulSiirtoOperationData(): Option[SiirtotiedostoOperation] = {
    koutaLightSiirtotiedostoDAO.getLatestSiirtotiedostoOperationData
  }

  def saveSiirtoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): Int = {
    koutaLightSiirtotiedostoDAO.saveSiirtotiedostoOperationData(siirtotiedostoOperation)
  }
}
