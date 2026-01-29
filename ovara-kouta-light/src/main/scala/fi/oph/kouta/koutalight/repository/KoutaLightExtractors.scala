package fi.oph.kouta.koutalight.repository

import fi.oph.kouta.domain.Kieli
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.koutalight.domain.{Kielistetty, KoutaLightKoulutusMetadata, KoutaLightKoulutusWithMetadata, SiirtotiedostoOperation}
import fi.oph.kouta.koutalight.util.KoutaLightJsonFormats
import org.json4s.jackson.Serialization.read
import slick.jdbc.GetResult

import java.util.UUID

trait KoutaLightExtractors extends KoutaLightJsonFormats {
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
      val createdAt    = r.nextTimestampOption().map(_.toInstant)
      val updatedAt = r.nextTimestampOption().map(_.toInstant) match {
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
        windowStart = r.nextTimestampOption().map(_.toInstant),
        windowEnd = r.nextTimestamp().toInstant,
        runStart = r.nextTimestamp().toInstant,
        runEnd = r.nextTimestamp().toInstant,
        storedEntitiesCount = r.nextInt()
      )
    })
}
