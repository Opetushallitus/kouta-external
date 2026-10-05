package fi.oph.kouta.external.auditlog

import com.google.gson.{JsonElement, JsonParser}
import fi.oph.kouta.external.util.KoutaJsonFormats
import org.json4s.jackson.Serialization.write

trait GsonSupport extends KoutaJsonFormats {

  def toGson[T <: AnyRef](entity: T): JsonElement =
    JsonParser.parseString(write[T](entity))
}
