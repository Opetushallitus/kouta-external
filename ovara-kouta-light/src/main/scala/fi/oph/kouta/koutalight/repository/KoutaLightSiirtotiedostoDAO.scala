package fi.oph.kouta.koutalight.repository

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.SQLHelpers
import fi.oph.kouta.external.domain.Kielistetty
import fi.oph.kouta.external.domain.koutalight.{KoutaLightKoulutusMetadata, KoutaLightKoulutusWithMetadata}
import fi.oph.kouta.koutalight.OvaraKoutaLightConfiguration
import fi.oph.kouta.koutalight.database.KoutaDatabaseConnection
import org.json4s.jackson.Serialization.read
import slick.dbio.DBIO
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object KoutaLightSiirtotiedostoDAO extends KoutaLightSiirtotiedostoDAO

class KoutaLightSiirtotiedostoDAO extends KoutaLightSiirtotiedostoSQL {
  def getKoulutukset(
      operationWindowStartTime: Option[LocalDateTime],
      operationWindowEndTime: LocalDateTime,
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

sealed trait KoutaLightSiirtotiedostoSQL extends SQLHelpers {
  private val SiirtotiedostoDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
  private val maxNumberOfItemsInFile       = OvaraKoutaLightConfiguration.s3Configuration.transferFileMaxItemCount;

  private def extractKielivalinta(json: Option[String]): Seq[Kieli] = json.map(read[Seq[Kieli]]).getOrElse(Seq())
  private def extractKielistetty(json: Option[String]): Kielistetty =
    json.map(read[Map[Kieli, String]]).getOrElse(Map())

  implicit val getKoutaLightKoulutusResult: GetResult[KoutaLightKoulutusWithMetadata] =
    GetResult(r => {
      val id           = Some(UUID.fromString(r.nextString()))
      val externalId   = r.nextString()
      val kielivalinta = extractKielivalinta(r.nextStringOption())
      val tila         = r.nextString()
      val nimi         = extractKielistetty(r.nextStringOption())
      val tarjoajat    = r.nextStringOption().map(read[List[Kielistetty]]).getOrElse(List())
      val metadata     = r.nextStringOption().map(read[KoutaLightKoulutusMetadata]).get
      val ownerOrg     = OrganisaatioOid(r.nextString())
      val createdAt    = r.nextTimestampOption().map(_.toLocalDateTime)
      val updatedAt = r.nextTimestampOption().map(_.toLocalDateTime) match {
        case Some(updatedAt) => Some(updatedAt)
        case None            => createdAt
      }

      KoutaLightKoulutusWithMetadata(
        id = id,
        externalId = externalId,
        kielivalinta = kielivalinta,
        tila = tila,
        nimi = nimi,
        tarjoajat = tarjoajat,
        metadata = metadata,
        ownerOrg = ownerOrg,
        createdAt = createdAt,
        updatedAt = updatedAt
      )
    })

  implicit val getLatestSiirtotiedostoOperationResult: GetResult[SiirtotiedostoOperation] =
    GetResult(r => {
      SiirtotiedostoOperation(
        id = UUID.fromString(r.nextString()),
        windowStart = r.nextTimestampOption().map(_.toLocalDateTime),
        windowEnd = r.nextTimestamp().toLocalDateTime,
        runStart = r.nextTimestamp().toLocalDateTime,
        runEnd = r.nextTimestamp().toLocalDateTime,
        storedEntitiesCount = r.nextInt()
      )
    })

  def selectKoulutukset(
      windowStartTime: Option[LocalDateTime],
      windowEndTime: LocalDateTime,
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
              WHERE ((created_at > $startTime OR updated_at > $startTime) AND (created_at < $endTime OR updated_at < $endTime))
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
    def formatTimeStamp(value: Option[LocalDateTime]) = value.map(SiirtotiedostoDateTimeFormat.format).orNull

    sqlu"""insert into siirtotiedosto_operaatio
          (id, window_start, window_end, run_start, run_end, stored_entities_count)
          values
            (${siirtotiedostoOperation.id.toString}::uuid,
            ${formatTimeStamp(siirtotiedostoOperation.windowStart)}::timestamp,
            ${formatTimeStamp(Some(siirtotiedostoOperation.windowEnd))}::timestamp,
            ${formatTimeStamp(Some(siirtotiedostoOperation.runStart))}::timestamp,
            ${formatTimeStamp(Some(siirtotiedostoOperation.runEnd))}::timestamp,
            ${siirtotiedostoOperation.storedEntitiesCount}
          )"""
  }
}

case class SiirtotiedostoOperation(
    id: UUID,
    windowStart: Option[LocalDateTime],
    windowEnd: LocalDateTime,
    runStart: LocalDateTime,
    runEnd: LocalDateTime,
    storedEntitiesCount: Int
)
