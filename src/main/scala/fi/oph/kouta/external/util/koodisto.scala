package fi.oph.kouta.external.util

import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.vm.sade.properties.OphProperties

object KoodistoUtil {
  lazy val urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties

  def createKoodistoLink(koodistoNimi: String) = urlProperties.url("koodisto-service.koodisto-link", koodistoNimi)

  def markdownKoodistoLink(koodistoNimi: String) =
    s"[${koodistoNimi}](${createKoodistoLink(koodistoNimi)})"
}
