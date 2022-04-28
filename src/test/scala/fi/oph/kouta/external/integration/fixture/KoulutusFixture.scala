package fi.oph.kouta.external.integration.fixture

import java.util.UUID
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.KoulutusClient
import fi.oph.kouta.external.service.{KoulutusService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.KoulutusServlet
import fi.oph.kouta.external.TempElasticClient

trait KoulutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val KoulutusPath = "/koulutus"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val koulutusService     = new KoulutusService(new KoulutusClient(TempElasticClient.client), organisaatioService)
    addServlet(new KoulutusServlet(koulutusService), KoulutusPath)
  }

  def get(oid: KoulutusOid): Koulutus = get[Koulutus](KoulutusPath, oid)

  def get(oid: KoulutusOid, sessionId: UUID): Koulutus = get[Koulutus](KoulutusPath, oid, sessionId)

  def get(oid: KoulutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$KoulutusPath/$oid", sessionId, errorStatus)

  def get(oid: KoulutusOid, sessionId: UUID, expected: Koulutus): String =
    get[Koulutus, KoulutusOid](KoulutusPath, oid, sessionId, expected)
}
