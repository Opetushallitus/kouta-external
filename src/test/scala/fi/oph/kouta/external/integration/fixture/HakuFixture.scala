package fi.oph.kouta.external.integration.fixture

import java.time.Instant
import java.util.UUID
import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.domain.Julkaisutila
import fi.oph.kouta.external.TestData.JulkaistuHaku
import fi.oph.kouta.external._
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.elasticsearch.HakuClient
import fi.oph.kouta.external.service.{HakuService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.HakuServlet

trait HakuFixture extends KoutaIntegrationSpec with AccessControlSpec {
  this: AccessControlSpec =>
  val HakuPath = "/haku"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val hakuService = new HakuService(
      new HakuClient(TempElasticClient.client),
      new MockKoutaClient(urlProperties.get),
      organisaatioService
    )
    addServlet(new HakuServlet(hakuService), HakuPath)
  }

  val haku = JulkaistuHaku

  def haku(oid: String): Haku = haku.copy(oid = Some(HakuOid(oid)))

  def haku(oid: String, tila: Julkaisutila): Haku = haku.copy(oid = Some(HakuOid(oid)), tila = tila)

  def haku(organisaatioOid: OrganisaatioOid): Haku =
    haku.copy(organisaatioOid = organisaatioOid)

  def haku(oid: String, organisaatioOid: OrganisaatioOid): Haku =
    haku.copy(oid = Some(HakuOid(oid)), organisaatioOid = organisaatioOid)

  def get(oid: HakuOid): Haku = get[Haku](HakuPath, oid)

  def get(oid: HakuOid, sessionId: UUID): Haku = get[Haku](HakuPath, oid, sessionId)

  def get(oid: HakuOid, errorStatus: Int): Unit = get(oid, defaultSessionId, errorStatus)

  def get(oid: HakuOid, sessionId: UUID, errorStatus: Int): Unit = get(s"$HakuPath/$oid", sessionId, errorStatus)

  def create(oid: String, organisaatioOid: OrganisaatioOid): String =
    create(HakuPath, haku(oid, organisaatioOid), parseOid)

  def create(organisaatioOid: OrganisaatioOid, expectedStatus: Int, expectedBody: String): Unit =
    create(HakuPath, haku(organisaatioOid), defaultSessionId, expectedStatus, expectedBody)

  def create(organisaatioOid: OrganisaatioOid, sessionId: UUID): String =
    create(HakuPath, haku(organisaatioOid), sessionId, parseOid)

  def update(oid: String, ifUnmodifiedSince: Instant): Unit =
    update(HakuPath, haku(oid), ifUnmodifiedSince)

  def update(oid: String, ifUnmodifiedSince: Option[Instant], expectedStatus: Int, expectedBody: String): Unit =
    ifUnmodifiedSince match {
      case Some(ifUnmodifiedSinceVal) =>
        update(HakuPath, haku(oid), ifUnmodifiedSinceVal, defaultSessionId, expectedStatus, expectedBody)
      case _ => update(HakuPath, haku(oid), defaultSessionId, expectedStatus, expectedBody)
    }

  def update(oid: String, ifUnmodifiedSince: Instant, sessionId: UUID): Unit =
    update(HakuPath, haku(oid), ifUnmodifiedSince, sessionId)
}
