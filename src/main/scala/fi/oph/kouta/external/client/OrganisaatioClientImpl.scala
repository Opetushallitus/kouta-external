package fi.oph.kouta.external.client

import fi.oph.kouta.client.OrganisaatioClient
import fi.oph.kouta.external.KoutaConfigurationFactory

object OrganisaatioClientImpl extends OrganisaatioClient(KoutaConfigurationFactory.configuration.urlProperties, "kouta-external")
