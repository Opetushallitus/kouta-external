package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.{Haku => BackendHaku}
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.util.{KoutaJsonFormats => BackendJsonFormats}
import org.json4s.{Extraction, JValue}

object KoutaBackendConverters extends KoutaJsonFormats {

  def convertHaku(haku: Haku): JValue =
    KoutaBackendParser.parseHaku(Extraction.decompose(haku))
}

object KoutaBackendParser extends BackendJsonFormats {

  def parseHaku(haku: JValue): JValue =
    Extraction.decompose(haku.extract[BackendHaku])
}
