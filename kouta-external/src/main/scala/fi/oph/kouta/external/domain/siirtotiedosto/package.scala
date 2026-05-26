package fi.oph.kouta.external.domain

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.UUID

package object siirtotiedosto {
  val SiirtotiedostoInstantFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of("Europe/Helsinki"))

  case class SiirtotiedostoOperation(
      id: UUID,
      windowStart: Option[Instant],
      windowEnd: Instant,
      runStart: Instant,
      runEnd: Instant,
      storedEntitiesCount: Int
  )
}
