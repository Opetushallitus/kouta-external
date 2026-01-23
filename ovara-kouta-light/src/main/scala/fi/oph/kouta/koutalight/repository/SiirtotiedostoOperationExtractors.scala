package fi.oph.kouta.koutalight.repository

import fi.oph.kouta.koutalight.domain.SiirtotiedostoOperation
import slick.jdbc.GetResult

import java.util.UUID

trait SiirtotiedostoOperationExtractors {
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
