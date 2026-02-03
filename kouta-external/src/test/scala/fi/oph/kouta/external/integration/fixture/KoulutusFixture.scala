package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid}
import fi.oph.kouta.external.TestData.AmmKoulutus
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.elasticsearch.{EPerusteClient, KoulutusClient}
import fi.oph.kouta.external.integration.GenericGetTests
import fi.oph.kouta.external.service.{KoulutusService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.KoulutusServlet
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient}
import fi.oph.kouta.servlet.Authenticated

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

trait KoulutusFixture extends AccessControlSpec {
  this: KoutaIntegrationSpec with GenericGetTests[Koulutus, KoulutusOid] =>
  val KoulutusPath = "/koulutus"

  val koulutus = AmmKoulutus

  override def beforeAll(): Unit = {
    super.beforeAll()
    val koulutusService = new KoulutusService(
      new KoulutusClient(TempElasticClient.client, TempElasticClient.clientJava),
      new EPerusteClient(TempElasticClient.client, TempElasticClient.clientJava),
      new MockKoutaClient(urlProperties.get),
      new OrganisaatioServiceImpl(urlProperties.get)
    ) {
      override def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] =
        throwOrElse(oid)(super.get)
    }
    addServlet(new KoulutusServlet(koulutusService), KoulutusPath)
  }

  def koulutus(oid: String): Koulutus =
    koulutus.copy(oid = Some(KoulutusOid(oid)))

  def koulutus(organisaatioOid: OrganisaatioOid): Koulutus =
    koulutus.copy(organisaatioOid = organisaatioOid)

  def koulutus(oid: String, organisaatioOid: OrganisaatioOid): Koulutus =
    koulutus.copy(oid = Some(KoulutusOid(oid)), organisaatioOid = organisaatioOid)

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

  def update(oid: String, ifUnmodifiedSince: Instant): Unit =
    update(KoulutusPath, koulutus(oid), ifUnmodifiedSince)

  def update(oid: String, ifUnmodifiedSince: Option[Instant], expectedStatus: Int, expectedBody: String): Unit =
    ifUnmodifiedSince match {
      case Some(ifUnmodifiedSinceVal) =>
        update(KoulutusPath, koulutus(oid), ifUnmodifiedSinceVal, defaultSessionId, expectedStatus, expectedBody)
      case _ => update(KoulutusPath, koulutus(oid), defaultSessionId, expectedStatus, expectedBody)
    }

  def update(oid: String, ifUnmodifiedSince: Instant, sessionId: UUID): Unit =
    update(KoulutusPath, koulutus(oid), ifUnmodifiedSince, sessionId)
}
