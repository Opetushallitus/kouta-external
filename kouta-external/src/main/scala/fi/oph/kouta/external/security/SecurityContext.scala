package fi.oph.kouta.external.security

import fi.oph.kouta.external.SecurityConfiguration
import fi.oph.kouta.external.kouta.CallerId
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}

trait SecurityContext {
  def casUrl: String
  def casServiceIdentifier: String
  def casClient: CasClient
}

case class ProductionSecurityContext(casUrl: String, casClient: CasClient, casServiceIdentifier: String)
    extends SecurityContext

object ProductionSecurityContext extends CallerId {
  def apply(config: SecurityConfiguration): ProductionSecurityContext = {
    val casClient =
      CasClientBuilder.build(new CasConfig.CasConfigBuilder("", "", config.casUrl, "", callerId, callerId, "").build())
    ProductionSecurityContext(config.casUrl, casClient, config.casServiceIdentifier)
  }
}
