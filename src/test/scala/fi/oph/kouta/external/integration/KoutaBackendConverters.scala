package fi.oph.kouta.external.integration

import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.util.{KoutaBackendJsonAdapter, KoutaJsonFormats}
import org.json4s.{Extraction, JValue}

object KoutaBackendConverters extends KoutaJsonFormats with KoutaBackendJsonAdapter {

  def convertKoulutus(koulutus: Koulutus): JValue =
    Extraction.decompose(koulutus)

  def convertToteutus(toteutus: Toteutus): JValue =
    Extraction.decompose(toteutus)

  def convertHakukohde(hakukohde: Hakukohde): JValue = {
    adaptHakukohdeJson(Extraction.decompose(hakukohde))
  }

  def convertValintaperuste(valintaperuste: Valintaperuste): JValue = {
    Extraction.decompose(valintaperuste)
  }
}
