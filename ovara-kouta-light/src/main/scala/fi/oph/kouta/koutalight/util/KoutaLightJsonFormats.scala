package fi.oph.kouta.koutalight.util

import fi.oph.kouta.koutalight.SiirtotiedostoApp.SiirtotiedostoInstantFormat
import fi.oph.kouta.util.GenericKoutaJsonFormats
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.{CustomSerializer, Formats, JNull, MappingException}

import java.time.Instant
import scala.util.control.NonFatal

trait KoutaLightJsonFormats extends GenericKoutaJsonFormats {
  override implicit def jsonFormats: Formats = genericKoutaFormats ++ Seq(instantSerializer)

  private def instantSerializer = new CustomSerializer[Instant](_ => {
    (
      {
        case JObject(List((_, JString(i)))) =>
          try {
            Instant.parse(i)
          } catch {
            case NonFatal(e) =>
              throw MappingException(e.getMessage, new java.lang.IllegalArgumentException(e))
          }
        case JNull => null
      },
      { case i: Instant =>
        JString(SiirtotiedostoInstantFormat.format(i))
      }
    )
  })
}
