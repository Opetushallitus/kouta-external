package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.TestOids._
import fi.oph.kouta.client.OrganisaatioClient
import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.service.KoulutusService
import fi.oph.kouta.external.servlet.KoulutusServlet
import fi.oph.kouta.external.{KoutaConfigurationFactory, KoutaFixtureTool, TempElasticClient}

trait KoulutusFixture extends KoutaIntegrationSpec {
  val KoulutusPath = "/koulutus"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioClient = new OrganisaatioClient(KoutaConfigurationFactory.configuration.urlProperties, "kouta-external")
    val koulutusService = new KoulutusService(new KoulutusClient(TempElasticClient.client), organisaatioClient)
    addServlet(new KoulutusServlet(koulutusService), KoulutusPath)
  }

  def get(oid: KoulutusOid): Koulutus = get[Koulutus](KoulutusPath, oid)

  def get(oid: KoulutusOid, sessionId: UUID): Koulutus = get[Koulutus](KoulutusPath, oid, sessionId)

  def get(oid: KoulutusOid, sessionId: UUID, errorStatus: Int): Unit = get(s"$KoulutusPath/$oid", sessionId, errorStatus)

  def addMockKoulutus(koulutusOid: KoulutusOid, organisaatioOid: OrganisaatioOid = ChildOid): Unit = {
    val koulutus = KoutaFixtureTool.DefaultKoulutusScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addKoulutus(koulutusOid.s, koulutus)
    indexKoulutus(koulutusOid)
  }
}
