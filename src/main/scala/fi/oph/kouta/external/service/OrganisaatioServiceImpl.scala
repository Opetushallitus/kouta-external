package fi.oph.kouta.external.service

import fi.oph.kouta.external.CallerId
import fi.oph.kouta.client.CachedOrganisaatioHierarkiaClient
import fi.oph.kouta.domain.oid.RootOrganisaatioOid
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.service.OrganisaatioService
import fi.vm.sade.properties.OphProperties

object OrganisaatioServiceImpl extends OrganisaatioServiceImpl(KoutaConfigurationFactory.configuration.urlProperties)

class OrganisaatioServiceImpl(urlProperties: OphProperties) extends OrganisaatioService {
  override protected val cachedOrganisaatioHierarkiaClient: CachedOrganisaatioHierarkiaClient =
    new CachedOrganisaatioHierarkiaClient with CallerId {
      override val organisaatioUrl: String = urlProperties.url("organisaatio-service.organisaatio.oid.jalkelaiset", RootOrganisaatioOid.s)
    }
}
