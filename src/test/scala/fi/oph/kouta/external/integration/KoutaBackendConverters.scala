package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.{Haku => BackendHaku, Koulutus => BackendKoulutus, Toteutus => BackendToteutus}
import fi.oph.kouta.external.domain.{Haku, Koulutus, Toteutus}
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.util.{KoutaJsonFormats => BackendJsonFormats}
import org.json4s.{Extraction, JValue}

object KoutaBackendConverters extends KoutaJsonFormats {

  def convertKoulutus(koulutus: Koulutus): JValue =
    KoutaBackendParser.parseKoulutus(Extraction.decompose(koulutus))

  def convertToteutus(toteutus: Toteutus): JValue =
    KoutaBackendParser.parseToteutus(Extraction.decompose(toteutus))

  def convertHaku(haku: Haku): JValue =
    KoutaBackendParser.parseHaku(Extraction.decompose(haku))
}

object KoutaBackendParser extends BackendJsonFormats {

  def parseKoulutus(koulutus: JValue): JValue =
    removeExtraFields(Extraction.decompose(koulutus.extract[BackendKoulutus]))

  def parseToteutus(toteutus: JValue): JValue =
    removeExtraFields(Extraction.decompose(toteutus.extract[BackendToteutus]))

  def parseHaku(haku: JValue): JValue =
    Extraction.decompose(haku.extract[BackendHaku])

  private def removeExtraFields(entity: JValue): JValue =
    entity removeField {
      case ("esikatselu", _) => true
      case _ => false
    }
}