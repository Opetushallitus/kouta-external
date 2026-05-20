package fi.oph.kouta.koutalight.repository

import fi.oph.kouta.koutalight.domain.{KoutaLightKoulutus, SiirtotiedostoOperation}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID

class KoutaLightSiirtotiedostoDAO(
    dbConnection: KoutaExternalDatabaseConnection
) extends KoutaLightSiirtotiedostoSQL {
  def getKoulutukset(
      operationWindowStartTime: Option[Instant],
      operationWindowEndTime: Instant,
      lastFetchedKoulutusId: Option[UUID],
      maxNumberOfItemsInFile: Int
  ): Seq[KoutaLightKoulutus] = {
    dbConnection.runBlocking(
      selectKoulutukset(operationWindowStartTime, operationWindowEndTime, lastFetchedKoulutusId, maxNumberOfItemsInFile)
    )
  }

  def getLatestSiirtotiedostoOperationData: Option[SiirtotiedostoOperation] = {
    dbConnection.runBlocking(selectLatestSiirtotiedostoOperation()) match {
      case existingSiirtotiedostoOperation if existingSiirtotiedostoOperation.nonEmpty =>
        Option(existingSiirtotiedostoOperation.head)
      case _ => None
    }
  }

  def saveSiirtotiedostoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): Int = {
    dbConnection.runBlocking(persistSiirtoOperationData(siirtotiedostoOperation))
  }
}

sealed trait KoutaLightSiirtotiedostoSQL extends KoutaLightExtractors with SQLHelpers {

  def selectKoulutukset(
      windowStartTime: Option[Instant],
      windowEndTime: Instant,
      lastFetchedKoulutusId: Option[UUID],
      maxNumberOfItemsInFile: Int
  ): DBIO[Seq[KoutaLightKoulutus]] = {
    val selectKoulutusSql =
      sql"""SELECT id,
                   external_id,
                   kielivalinta,
                   tila,
                   nimi,
                   tarjoajat,
                   metadata,
                   owner_org,
                   created_at,
                   updated_at
            FROM kouta_light_koulutus"""

    val startTimeEndTimeClause = windowStartTime match {
      case Some(startTime) => sql""" WHERE ((created_at >= $startTime AND created_at < $windowEndTime)
                                           OR (updated_at >= $startTime AND updated_at < $windowEndTime))"""
      case None            => sql" WHERE (created_at < $windowEndTime OR updated_at < $windowEndTime)"
    }

    val lastFetchedKoulutusClause = lastFetchedKoulutusId match {
      case Some(id) => sql" AND id > $id"
      case None     => sql""
    }

    val orderByAndLimitClause = sql""" ORDER BY id
                                       LIMIT $maxNumberOfItemsInFile"""

    (selectKoulutusSql concat
      startTimeEndTimeClause concat
      lastFetchedKoulutusClause concat
      orderByAndLimitClause).as[KoutaLightKoulutus]
  }

  def selectLatestSiirtotiedostoOperation(): DBIO[Seq[SiirtotiedostoOperation]] = {
    sql"""SELECT id, window_start, window_end, run_start, run_end, stored_entities_count
          FROM siirtotiedosto_operaatio
          ORDER by run_start DESC
         """.as[SiirtotiedostoOperation]
  }

  def persistSiirtoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): DBIO[Int] = {
    sqlu"""INSERT INTO siirtotiedosto_operaatio
          (id, window_start, window_end, run_start, run_end, stored_entities_count)
          VALUES
            (${siirtotiedostoOperation.id.toString}::uuid,
            ${siirtotiedostoOperation.windowStart}::timestamp,
            ${siirtotiedostoOperation.windowEnd}::timestamp,
            ${siirtotiedostoOperation.runStart}::timestamp,
            ${siirtotiedostoOperation.runEnd}::timestamp,
            ${siirtotiedostoOperation.storedEntitiesCount}
          )"""
  }
}
