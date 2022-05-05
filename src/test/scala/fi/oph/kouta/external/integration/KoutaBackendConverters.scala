package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.{Haku => BackendHaku, Hakukohde => BackendHakukohde, Koulutus => BackendKoulutus, Sorakuvaus => BackendSorakuvaus, Toteutus => BackendToteutus, Valintaperuste => BackendValintaperuste}
import fi.oph.kouta.external.domain._
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

  def convertHakukohde(hakukohde: Hakukohde): JValue = {
    KoutaBackendParser.parseHakukohde(Extraction.decompose(hakukohde))
  }

  def convertValintaperuste(valintaperuste: Valintaperuste): JValue =
    KoutaBackendParser.parseValintaperuste(Extraction.decompose(valintaperuste))

  def convertSorakuvaus(sorakuvaus: Sorakuvaus): JValue =
    KoutaBackendParser.parseSorakuvaus(Extraction.decompose(sorakuvaus))
}

object KoutaBackendParser extends BackendJsonFormats {

  def parseKoulutus(koulutus: JValue): JValue =
    Extraction.decompose(koulutus.extract[BackendKoulutus])

  def parseToteutus(toteutus: JValue): JValue =
    Extraction.decompose(toteutus.extract[BackendToteutus])

  def parseHaku(haku: JValue): JValue =
    Extraction.decompose(haku.extract[BackendHaku])

  def parseHakukohde(hakukohde: JValue): JValue = {
    Extraction.decompose(hakukohde.extract[BackendHakukohde])
  }

  def parseValintaperuste(valintaperuste: JValue): JValue = {
    Extraction.decompose(valintaperuste.extract[BackendValintaperuste])
  }

  def parseSorakuvaus(sorakuvaus: JValue): JValue = {
    Extraction.decompose(sorakuvaus.extract[BackendSorakuvaus])
  }
}
