package fi.oph.kouta.koutalight.repository

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.database.{KoutaDatabase, SQLHelpers}
import fi.oph.kouta.external.domain.Kielistetty
import fi.oph.kouta.external.domain.koutalight.{KoutaLightKoulutusMetadata, KoutaLightKoulutusWithMetadata}
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
  def getKoulutukset(lastOperationWindowEndTime: Option[LocalDateTime]): Seq[KoutaLightKoulutusWithMetadata] = {
    KoutaDatabaseConnection.runBlocking(selectKoulutukset(lastOperationWindowEndTime))
  }

  def getLatestSiirtotiedostoOperationData: Option[SiirtotiedostoOperation] = {
    KoutaDatabaseConnection.runBlocking(selectLatestSiirtotiedostoOperation()) match {
      case existingSiirtotiedostoOperation if existingSiirtotiedostoOperation.nonEmpty => Option(existingSiirtotiedostoOperation.head)
      case _ => None
    }
  }

  def saveSiirtotiedostoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): Int = {
    KoutaDatabaseConnection.runBlocking(persistSiirtoOperationData(siirtotiedostoOperation))
  }
}

sealed trait KoutaLightSiirtotiedostoSQL extends SQLHelpers {
  val SiirtotiedostoDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

  private def extractKielivalinta(json: Option[String]): Seq[Kieli] = json.map(read[Seq[Kieli]]).getOrElse(Seq())
  private def extractKielistetty(json: Option[String]): Kielistetty =
    json.map(read[Map[Kieli, String]]).getOrElse(Map())

  implicit val getKoutaLightKoulutusResult: GetResult[KoutaLightKoulutusWithMetadata] =
    GetResult(r => {
      KoutaLightKoulutusWithMetadata(
        externalId = r.nextString(),
        kielivalinta = extractKielivalinta(r.nextStringOption()),
        tila = r.nextString(),
        nimi = extractKielistetty(r.nextStringOption()),
        tarjoajat = r.nextStringOption().map(read[List[Kielistetty]]).getOrElse(List()),
        metadata = r.nextStringOption().map(read[KoutaLightKoulutusMetadata]).get,
        ownerOrg = OrganisaatioOid(r.nextString())
      )
    })

  implicit val getLatestSiirtotiedostoOperationResult: GetResult[SiirtotiedostoOperation] =
    GetResult(r => {
      SiirtotiedostoOperation(
        id = UUID.fromString(r.nextString()),
        windowStart = r.nextTimestampOption().map(_.toLocalDateTime),
        windowEnd = r.nextTimestamp().toLocalDateTime,
        runStart = r.nextTimestamp().toLocalDateTime,
        runEnd = r.nextTimestamp().toLocalDateTime
      )
    })

  def selectKoulutukset(lastModified: Option[LocalDateTime]): DBIO[Seq[KoutaLightKoulutusWithMetadata]] = {
    // kokeile lastModified tilalle skripti tms.
    val lastModifiedFilter = lastModified match {
      case Some(lastModified) => s"where created_at > '$lastModified' or updated_at > '$lastModified'"
      case None => ""
    }

    println(lastModifiedFilter)
    sql"""select external_id,
                 kielivalinta,
                 tila,
                 nimi,
                 tarjoajat,
                 metadata,
                 owner_org,
                 created_at,
                 updated_at
          from kouta_light_koulutus #$lastModifiedFilter"""
      .as[KoutaLightKoulutusWithMetadata]
  }

  def selectLatestSiirtotiedostoOperation(): DBIO[Seq[SiirtotiedostoOperation]] = {
    sql"""select id, window_start, window_end, run_start, run_end
          from siirtotiedosto_operaatio
          order by run_start DESC
         """.as[SiirtotiedostoOperation]
  }

  def persistSiirtoOperationData(siirtotiedostoOperation: SiirtotiedostoOperation): DBIO[Int] = {
    sqlu"""insert into siirtotiedosto_operaatio
          (id, window_start, window_end, run_start, run_end)
          values
            (${siirtotiedostoOperation.id.toString}::uuid,
            ${siirtotiedostoOperation.windowStart.map(SiirtotiedostoDateTimeFormat.format)}::timestamp,
            ${SiirtotiedostoDateTimeFormat.format(siirtotiedostoOperation.windowEnd)}::timestamp,
            ${formatTimestampParam(Some(siirtotiedostoOperation.runStart))}::timestamp,
            ${formatTimestampParam(Some(siirtotiedostoOperation.runEnd))}::timestamp
          )"""
  }
}

case class SiirtotiedostoOperation(
    id: UUID,
    windowStart: Option[LocalDateTime],
    windowEnd: LocalDateTime,
    runStart: LocalDateTime,
    runEnd: LocalDateTime
)
