package fi.oph.kouta.koutalight.repository

import fi.oph.kouta.koutalight.util.KoutaLightJsonFormats
import slick.jdbc.{PositionedParameters, SetParameter}

import java.sql.JDBCType
import java.time.{Instant, OffsetDateTime, ZoneId}

trait SQLHelpers extends KoutaLightJsonFormats {
  implicit object SetInstant extends SetParameter[Instant] {
    def apply(v: Instant, pp: PositionedParameters): Unit = {
      pp.setObject(
        OffsetDateTime.ofInstant(v, ZoneId.of("Europe/Helsinki")),
        JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber
      )
    }
  }

  implicit object SetInstantOption extends SetParameter[Option[Instant]] {
    def apply(v: Option[Instant], pp: PositionedParameters): Unit = {
      v match {
        case Some(i) => SetInstant.apply(i, pp)
        case None    => pp.setNull(java.sql.Types.NULL)
      }
    }
  }
}
