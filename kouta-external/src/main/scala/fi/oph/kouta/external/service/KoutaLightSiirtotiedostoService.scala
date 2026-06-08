package fi.oph.kouta.external.service

import fi.oph.kouta.external.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.external.database.KoutaLightSiirtotiedostoDAO
import fi.oph.kouta.external.domain.siirtotiedosto.SiirtotiedostoOperation

import java.time.Instant
import java.util.UUID

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

    var operationSubId        = 1
    var keyList: List[String] = List()
    var koulutusIds           = koulutukset.flatMap(_.id)

    while (koulutukset.nonEmpty) {
      keyList = keyList :+ siirtotiedostoPalveluClient.saveSiirtotiedosto(
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
      operationSubId += 1
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
