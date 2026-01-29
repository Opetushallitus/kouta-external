package fi.oph.kouta.koutalight.repository

import fi.oph.kouta.koutalight.OvaraKoutaLightConfiguration
import fi.oph.kouta.koutalight.database.KoutaDatabaseConnection
import fi.oph.kouta.koutalight.domain.{KoutaLightKoulutusWithMetadata, SiirtotiedostoOperation}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID

object KoutaLightSiirtotiedostoDAO extends KoutaLightSiirtotiedostoDAO

class KoutaLightSiirtotiedostoDAO extends KoutaLightSiirtotiedostoSQL {
  def getKoulutukset(
      operationWindowStartTime: Option[Instant],
      operationWindowEndTime: Instant,
      lastFetchedKoulutusId: Option[UUID]
  ): Seq[KoutaLightKoulutusWithMetadata] = {
    KoutaDatabaseConnection.runBlocking(
      selectKoulutukset(operationWindowStartTime, operationWindowEndTime, lastFetchedKoulutusId)
    )
  }

  def getLatestSiirtotiedostoOperationData: Option[SiirtotiedostoOperation] = {
    KoutaDatabaseConnection.runBlocking(selectLatestSiirtotiedostoOperation()) match {
      case existingSiirtotiedostoOperation if existingSiirtotiedostoOperation.nonEmpty =>
        Option(existingSiirtotiedostoOperation.head)
      case _ => None
    }
  }

  def saveSiirtotiedostoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): Int = {
    KoutaDatabaseConnection.runBlocking(persistSiirtoOperationData(siirtotiedostoOperation))
  }
}

sealed trait KoutaLightSiirtotiedostoSQL extends Extractors with SiirtotiedostoOperationExtractors with SQLHelpers {
  private val maxNumberOfItemsInFile = OvaraKoutaLightConfiguration.s3Configuration.transferFileMaxItemCount;

  def selectKoulutukset(
      windowStartTime: Option[Instant],
      windowEndTime: Instant,
      lastFetchedKoulutusId: Option[UUID]
  ): DBIO[Seq[KoutaLightKoulutusWithMetadata]] = {
    val selectKoulutusSql =
      """SELECT id,
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

    val lastFetchedKoulutusClause = lastFetchedKoulutusId match {
      case Some(id) => s"AND id > '$id'"
      case None     => ""
    }

    val orderByAndLimitClause = s"""ORDER BY id
                                    LIMIT $maxNumberOfItemsInFile"""

    (windowStartTime, windowEndTime) match {
      case (Some(startTime), endTime) =>
        sql"""#$selectKoulutusSql
              WHERE ((created_at >= $startTime AND created_at < $endTime) OR (updated_at >= $startTime AND updated_at < $endTime))
              #$lastFetchedKoulutusClause
              #$orderByAndLimitClause""".as[KoutaLightKoulutusWithMetadata]
      case (None, endTime) =>
        sql"""#$selectKoulutusSql
              WHERE (created_at < $endTime OR updated_at < $endTime)
              #$lastFetchedKoulutusClause
              #$orderByAndLimitClause""".as[KoutaLightKoulutusWithMetadata]
    }
  }

  def selectLatestSiirtotiedostoOperation(): DBIO[Seq[SiirtotiedostoOperation]] = {
    sql"""select id, window_start, window_end, run_start, run_end, stored_entities_count
          from siirtotiedosto_operaatio
          order by run_start DESC
         """.as[SiirtotiedostoOperation]
  }

  def persistSiirtoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): DBIO[Int] = {
    sqlu"""insert into siirtotiedosto_operaatio
          (id, window_start, window_end, run_start, run_end, stored_entities_count)
          values
            (${siirtotiedostoOperation.id.toString}::uuid,
            ${siirtotiedostoOperation.windowStart}::timestamp,
            ${siirtotiedostoOperation.windowEnd}::timestamp,
            ${siirtotiedostoOperation.runStart}::timestamp,
            ${siirtotiedostoOperation.runEnd}::timestamp,
            ${siirtotiedostoOperation.storedEntitiesCount}
          )"""
  }
}
