package fi.oph.kouta.external.integration.fixture

import java.util.UUID
import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.service.{KoulutusService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.KoulutusServlet
import fi.oph.kouta.external.TestData.AmmKoulutus
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient}

trait KoulutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val KoulutusPath = "/koulutus"

  val koulutus = AmmKoulutus

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val koulutusService     = new KoulutusService(new KoulutusClient(TempElasticClient.client), new MockKoutaClient(urlProperties.get), organisaatioService)
    addServlet(new KoulutusServlet(koulutusService), KoulutusPath)
  }

  def koulutus(organisaatioOid: OrganisaatioOid): Koulutus =
    koulutus.copy(organisaatioOid = organisaatioOid)

  def koulutus(oid: String, organisaatioOid: OrganisaatioOid): Koulutus =
    koulutus.copy(oid = Some(KoulutusOid(oid)), organisaatioOid = organisaatioOid)

  def koulutus(organisaatioOid: String): Koulutus = koulutus(OrganisaatioOid(organisaatioOid))

  def get(oid: KoulutusOid): Koulutus = get[Koulutus](KoulutusPath, oid)

  def get(oid: KoulutusOid, sessionId: UUID): Koulutus = get[Koulutus](KoulutusPath, oid, sessionId)

  def get(oid: KoulutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$KoulutusPath/$oid", sessionId, errorStatus)

  def get(oid: KoulutusOid, sessionId: UUID, expected: Koulutus): String =
    get[Koulutus, KoulutusOid](KoulutusPath, oid, sessionId, expected)

  def create(oid: String, organisaatioOid: OrganisaatioOid): String =
    create(KoulutusPath, koulutus(oid, organisaatioOid), parseOid)

  def create(organisaatioOid: OrganisaatioOid, expectedStatus: Int, expectedBody: String): Unit =
    create(KoulutusPath, koulutus(organisaatioOid), defaultSessionId, expectedStatus, expectedBody)

  def create(organisaatioOid: OrganisaatioOid, sessionId: UUID): String =
    create(KoulutusPath, koulutus(organisaatioOid), sessionId, parseOid)

}
