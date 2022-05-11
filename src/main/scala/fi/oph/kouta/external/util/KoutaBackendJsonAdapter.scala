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

  private def adaptKoulutusJson(koulutusJson: JValue): JValue = {
    addDefaultEsikatselu("koulutus", koulutusJson)
  }

  private def adaptToteutusJson(toteutusJson: JValue): JValue = {
    addDefaultEsikatselu("toteutus", toteutusJson)
  }

  private def adaptHakukohdeJson(hakukohdeJson: JValue): JValue = {
    val adapted = addDefaultEsikatselu("hakukohde", hakukohdeJson)
    adapted mapField {
      case ("tarjoaja", JString(org)) =>
        ("jarjestyspaikkaOid", JString(org))
      case other => other
    }
  }

  private def adaptValintaperusteJson(valintaperusteJson: JValue): JValue = {
    addDefaultEsikatselu("valintaperuste", valintaperusteJson)
  }

  private def addDefaultEsikatselu(entityName: String, json: JValue): JValue =
    json mapField {
      case (entityName, JObject(elems: List[(String, JValue)])) =>
        (entityName, JObject(elems ++ List(("esikatselu", JBool(false)))))
      case other => other
    }
}
