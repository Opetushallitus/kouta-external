package fi.oph.kouta.external.security

import fi.oph.kouta.external.SecurityConfiguration
import fi.oph.kouta.external.kouta.CallerId
import fi.oph.kouta.external.util.ScalaCasConfig
import fi.vm.sade.javautils.nio.cas.CasClient

trait SecurityContext {
  def casUrl: String
  def casServiceIdentifier: String
  def casClient: CasClient
}

case class ProductionSecurityContext(casUrl: String, casClient: CasClient, casServiceIdentifier: String)
    extends SecurityContext

object ProductionSecurityContext extends CallerId {
  def apply(config: SecurityConfiguration): ProductionSecurityContext = {
    val casClient = new CasClient(ScalaCasConfig(
      config.username,
      config.password,
      config.casUrl,
      config.kayttooikeusUrl,
      csrf = callerId, callerId = callerId,
      serviceUrlSuffix = "/j_spring_cas_security_check",
      jSessionName = "JSESSIONID"
    ))
    ProductionSecurityContext(config.casUrl, casClient, config.casServiceIdentifier)
  }
}
