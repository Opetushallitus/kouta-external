package fi.oph.kouta.koutalight.domain

import java.time.Instant
import java.util.UUID

case class SiirtotiedostoOperation(
    id: UUID,
    windowStart: Option[Instant],
    windowEnd: Instant,
    runStart: Instant,
    runEnd: Instant,
    storedEntitiesCount: Int
)
