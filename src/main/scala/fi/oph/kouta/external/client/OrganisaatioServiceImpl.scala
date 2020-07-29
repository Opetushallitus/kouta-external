package fi.oph.kouta.external.client

import fi.oph.kouta.client.{CachedOrganisaatioHierarkiaClient, OrganisaatioService}
import fi.oph.kouta.domain.oid.RootOrganisaatioOid
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.vm.sade.properties.OphProperties

object OrganisaatioServiceImpl extends OrganisaatioServiceImpl(KoutaConfigurationFactory.configuration.urlProperties)

class OrganisaatioServiceImpl(urlProperties: OphProperties) extends OrganisaatioService {
  override protected val cachedOrganisaatioHierarkiaClient: CachedOrganisaatioHierarkiaClient =
    new CachedOrganisaatioHierarkiaClient {
      override val organisaatioUrl: String = urlProperties.url("organisaatio-service.organisaatio.oid.jalkelaiset", RootOrganisaatioOid.s)

      override def callerId: String = "kouta-backend"
    }
}
