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
      case k: KoutaKoulutusRequest        => adaptKoulutusJson(koutaExternalJson)
      case t: KoutaToteutusRequest        => adaptToteutusJson(koutaExternalJson)
      case hk: KoutaHakukohdeRequest      => adaptHakukohdeJson(koutaExternalJson)
      case vp: KoutaValintaperusteRequest => adaptValintaperusteJson(koutaExternalJson)
      case _                              => koutaExternalJson
    }
  }

  protected def adaptKoulutusJson(koulutusJson: JValue): JValue = {
    addDefaultEsikatselu(koulutusJson)
  }

  protected def adaptToteutusJson(toteutusJson: JValue): JValue = {
    addDefaultEsikatselu(toteutusJson)
  }

  protected def adaptHakukohdeJson(hakukohdeJson: JValue): JValue = {
    val adapted = addDefaultEsikatselu(hakukohdeJson)
    adapted mapField {
      case ("tarjoaja", JString(org)) =>
        ("jarjestyspaikkaOid", JString(org))
      case other => other
    }
  }

  protected def adaptValintaperusteJson(valintaperusteJson: JValue): JValue = {
    addDefaultEsikatselu(valintaperusteJson)
  }
  private def addDefaultEsikatselu(json: JValue): JValue =
    addEntityFieldToJson(json, "esikatselu", JBool(false))

  private def addEntityFieldToJson(json: JValue, fieldName: String, fieldValue: JValue): JValue =
    json mapField {
      case ("koulutus", JObject(elems: List[(String, JValue)])) =>
        ("koulutus", JObject(elems ++ List((fieldName, fieldValue))))
      case ("toteutus", JObject(elems: List[(String, JValue)])) =>
        ("toteutus", JObject(elems ++ List((fieldName, fieldValue))))
      case ("haku", JObject(elems: List[(String, JValue)])) =>
        ("haku", JObject(elems ++ List((fieldName, fieldValue))))
      case ("hakukohde", JObject(elems: List[(String, JValue)])) =>
        ("hakukohde", JObject(elems ++ List((fieldName, fieldValue))))
      case ("valintaperuste", JObject(elems: List[(String, JValue)])) =>
        ("valintaperuste", JObject(elems ++ List((fieldName, fieldValue))))
      case ("sorakuvaus", JObject(elems: List[(String, JValue)])) =>
        ("sorakuvaus", JObject(elems ++ List((fieldName, fieldValue))))
      case other => other
    }
}
