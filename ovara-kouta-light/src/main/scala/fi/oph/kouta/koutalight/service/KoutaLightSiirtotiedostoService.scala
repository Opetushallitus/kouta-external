package fi.oph.kouta.koutalight.service

import fi.oph.kouta.koutalight.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.koutalight.repository.{KoutaLightSiirtotiedostoDAO, SiirtotiedostoOperation}

import java.time.LocalDateTime
import java.util.UUID

object KoutaLightSiirtotiedostoService
    extends KoutaLightSiirtotiedostoService(KoutaLightSiirtotiedostoDAO, SiirtotiedostoPalveluClient)

class KoutaLightSiirtotiedostoService(
    koutaLightSiirtotiedostoDAO: KoutaLightSiirtotiedostoDAO,
    siirtotiedostoPalveluClient: SiirtotiedostoPalveluClient
) {
  def storeKoulutukset(operationId: UUID, lastOperationWindowEndTime: Option[LocalDateTime]) = {
    val koulutukset = koutaLightSiirtotiedostoDAO.getKoulutukset(lastOperationWindowEndTime)
    println(koulutukset)

    val res = siirtotiedostoPalveluClient.saveSiirtotiedosto(
      contentType = "koulutukset",
      content = koulutukset,
      operationId = operationId,
      operationSubId = 0
    )

    println(res)

    res
  }

  def findLatestSuccessfulSiirtoOperationData(): Option[SiirtotiedostoOperation] = {
    koutaLightSiirtotiedostoDAO.getLatestSiirtotiedostoOperationData
  }

  def saveSiirtoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): Int = {
    koutaLightSiirtotiedostoDAO.saveSiirtotiedostoOperationData(siirtotiedostoOperation)
  }
}
