package fi.oph.kouta.external.auditlog

import com.google.gson.{JsonElement, JsonObject}
import fi.vm.sade.auditlog.Changes

import scala.collection.JavaConverters._

private[auditlog] object ChangeFactory {

  /**
   * Muodostaa muutokset vertaamalla JSON-objekteja rekursiivisesti. Sisäkkäisten kenttien polut
   * erotetaan pisteellä (esim. "metadata.kuvaus.fi"). Taulukoita verrataan kokonaisuuksina.
   */
  def getChanges(before: JsonElement, after: JsonElement): Changes = {
    val changes = new Changes.Builder()
    diff("", before, after, changes)
    changes.build()
  }

  private def diff(path: String, before: JsonElement, after: JsonElement, changes: Changes.Builder): Unit =
    (before, after) match {
      case (b: JsonObject, a: JsonObject) =>
        val fields = (b.keySet().asScala.toSeq ++ a.keySet().asScala.toSeq).distinct
        fields.foreach { field =>
          val fieldPath = if (path.isEmpty) field else s"$path.$field"
          (nonNull(b.get(field)), nonNull(a.get(field))) match {
            case (Some(oldValue), Some(newValue)) => diff(fieldPath, oldValue, newValue, changes)
            case (None, Some(newValue))           => changes.added(fieldPath, jsonString(newValue))
            case (Some(oldValue), None)           => changes.removed(fieldPath, jsonString(oldValue))
            case (None, None)                     =>
          }
        }
      case _ if before != after =>
        changes.updated(path, jsonString(before), jsonString(after))
      case _ =>
    }

  private def nonNull(json: JsonElement): Option[JsonElement] = Option(json).filterNot(_.isJsonNull)

  private def jsonString(json: JsonElement): String =
    if (json.isJsonPrimitive && json.getAsJsonPrimitive.isString) json.getAsString else json.toString
}
