package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.service.{KoulutusService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.KoulutusServlet
import fi.oph.kouta.external.{KoutaFixtureTool, TempElasticClient}

trait KoulutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val KoulutusPath = "/koulutus"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val koulutusService = new KoulutusService(new KoulutusClient(TempElasticClient.client), organisaatioService)
    addServlet(new KoulutusServlet(koulutusService), KoulutusPath)
  }

  def get(oid: KoulutusOid): Koulutus = get[Koulutus](KoulutusPath, oid)

  def get(oid: KoulutusOid, sessionId: UUID): Koulutus = get[Koulutus](KoulutusPath, oid, sessionId)

  def get(oid: KoulutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$KoulutusPath/$oid", sessionId, errorStatus)

  def addMockKoulutus(
      koulutusOid: KoulutusOid,
      organisaatioOid: OrganisaatioOid = ChildOid,
      modifier: Map[String, String] => Map[String, String] = identity
  ): Unit = {
    val koulutus = KoutaFixtureTool.DefaultKoulutusScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addKoulutus(koulutusOid.s, modifier(koulutus))
    indexKoulutus(koulutusOid)
  }
}
