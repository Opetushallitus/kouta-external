package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.OrganisaatioOid

import java.util.UUID
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.elasticsearch.ValintaperusteClient
import fi.oph.kouta.external.service.{OrganisaatioServiceImpl, ValintaperusteService}
import fi.oph.kouta.external.servlet.ValintaperusteServlet
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient}
import fi.oph.kouta.external.TestData.AmmValintaperuste
import fi.oph.kouta.external.integration.GenericGetTests

import java.time.Instant
import scala.concurrent.Future

trait ValintaperusteFixture extends KoutaIntegrationSpec with AccessControlSpec {
  this: GenericGetTests[Valintaperuste, UUID] =>
  val ValintaperustePath  = "/valintaperuste"
  val ValintaperusteIdKey = "valintaperuste"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val valintaperusteService = new ValintaperusteService(
      new ValintaperusteClient(TempElasticClient.client, TempElasticClient.clientJava) {
        override def getValintaperuste(id: UUID): Future[Valintaperuste] =
          throwOrElse(id)(super.getValintaperuste)
      },
      new MockKoutaClient(urlProperties.get),
      organisaatioService
    )
    addServlet(new ValintaperusteServlet(valintaperusteService), ValintaperustePath)
  }

  val valintaperuste = AmmValintaperuste

  def valintaperuste(id: String): Valintaperuste = valintaperuste.copy(id = Some(UUID.fromString(id)))

  def valintaperuste(organisaatioOid: OrganisaatioOid): Valintaperuste =
    valintaperuste.copy(organisaatioOid = organisaatioOid)

  def valintaperuste(id: String, organisaatioOid: OrganisaatioOid): Valintaperuste = {
    valintaperuste.copy(id = Some(UUID.fromString(id)), organisaatioOid = organisaatioOid)
  }

  def get(id: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id)

  def get(id: UUID, sessionId: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id, sessionId)

  def get(id: UUID, sessionId: UUID, errorStatus: Int): Unit = get(s"$ValintaperustePath/$id", sessionId, errorStatus)

  def get(id: UUID, sessionId: UUID, expected: Valintaperuste): String =
    get[Valintaperuste, UUID](ValintaperusteIdKey, id, sessionId, expected)

  def create(id: String, organisaatioOid: OrganisaatioOid): String =
    create(ValintaperustePath, valintaperuste(id, organisaatioOid), parseId)

  def create(organisaatioOid: OrganisaatioOid, expectedStatus: Int, expectedBody: String): Unit =
    create(ValintaperustePath, valintaperuste(organisaatioOid), defaultSessionId, expectedStatus, expectedBody)

  def create(organisaatioOid: OrganisaatioOid, sessionId: UUID): String =
    create(ValintaperustePath, valintaperuste(organisaatioOid), sessionId, parseId)

  def update(id: String, ifUnmodifiedSince: Instant): Unit =
    update(ValintaperustePath, valintaperuste(id), ifUnmodifiedSince)

  def update(id: String, ifUnmodifiedSince: Option[Instant], expectedStatus: Int, expectedBody: String): Unit =
    ifUnmodifiedSince match {
      case Some(ifUnmodifiedSinceVal) => update(ValintaperustePath, valintaperuste(id), ifUnmodifiedSinceVal, defaultSessionId, expectedStatus, expectedBody)
      case _ => update(ValintaperustePath, valintaperuste(id), defaultSessionId, expectedStatus, expectedBody)
    }

  def update(id: String, ifUnmodifiedSince: Instant, sessionId: UUID): Unit =
    update(ValintaperustePath, valintaperuste(id), ifUnmodifiedSince, sessionId)
}
