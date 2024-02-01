package fi.oph.kouta.external.util

import fi.oph.kouta.external.kouta.{
  KoutaHakukohdeRequest,
  KoutaKoulutusRequest,
  KoutaToteutusRequest,
  KoutaValintaperusteRequest
}
import org.json4s.JsonAST.JValue
import org.json4s.{JBool, JObject, JString}

trait KoutaBackendJsonAdapter {
  def adaptToKoutaBackendJson(entityType: Any, koutaExternalJson: JValue): JValue = {
    entityType match {
      case hk: KoutaHakukohdeRequest      => adaptHakukohdeJson(koutaExternalJson)
      case _                              => koutaExternalJson
    }
  }

  protected def adaptHakukohdeJson(hakukohdeJson: JValue): JValue = {
    hakukohdeJson mapField {
      case ("tarjoaja", JString(org)) =>
        ("jarjestyspaikkaOid", JString(org))
      case other => other
    }
  }


}
