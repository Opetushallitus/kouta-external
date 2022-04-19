package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.{Haku => BackendHaku, Koulutus => BackendKoulutus}
import fi.oph.kouta.external.domain.{Haku, Koulutus}
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.util.{KoutaJsonFormats => BackendJsonFormats}
import org.json4s.{Extraction, JValue}

object KoutaBackendConverters extends KoutaJsonFormats {

  def convertKoulutus(koulutus: Koulutus): JValue =
    KoutaBackendParser.parseKoulutus(Extraction.decompose(koulutus))

  def convertHaku(haku: Haku): JValue =
    KoutaBackendParser.parseHaku(Extraction.decompose(haku))
}

object KoutaBackendParser extends BackendJsonFormats {

  def parseKoulutus(koulutus: JValue): JValue = {
    Extraction.decompose(koulutus.extract[BackendKoulutus]) removeField {
      case ("esikatselu", _) => true
      case _ => false
    }
  }

  def parseHaku(haku: JValue): JValue =
    Extraction.decompose(haku.extract[BackendHaku])
}