package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.{OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.external.TestData.AmmToteutus
import fi.oph.kouta.external.{MockKoutaClient, TempElasticClient, TestData}
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.integration.GenericGetTests
import fi.oph.kouta.external.service.{OrganisaatioServiceImpl, ToteutusService}
import fi.oph.kouta.external.servlet.ToteutusServlet
import fi.oph.kouta.servlet.Authenticated

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

trait ToteutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  this: GenericGetTests[Toteutus, ToteutusOid] =>
  val ToteutusPath = "/toteutus"

  val toteutus = AmmToteutus

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val toteutusService = new ToteutusService(new ToteutusClient(TempElasticClient.client, TempElasticClient.clientJava), new MockKoutaClient(urlProperties.get), organisaatioService) {
      override def get(oid: ToteutusOid)(implicit authenticated: Authenticated): Future[Toteutus] =
        throwOrElse(oid)(super.get)
    }
    addServlet(new ToteutusServlet(toteutusService), ToteutusPath)
  }


  def toteutus(oid: String): Toteutus = toteutus.copy(oid = Some(ToteutusOid(oid)))

  def toteutus(oid: String, organisaatioOid: OrganisaatioOid): Toteutus =
    toteutus.copy(oid = Some(ToteutusOid(oid)), organisaatioOid = organisaatioOid)

  def toteutus(organisaatioOid: OrganisaatioOid): Toteutus =
    toteutus.copy(organisaatioOid = organisaatioOid)

  def get(oid: ToteutusOid): Toteutus = get[Toteutus](ToteutusPath, oid)

  def get(oid: ToteutusOid, sessionId: UUID): Toteutus = get[Toteutus](ToteutusPath, oid, sessionId)

  def get(oid: ToteutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$ToteutusPath/$oid", sessionId, errorStatus)

  def create(oid: String, organisaatioOid: OrganisaatioOid): String =
    create(ToteutusPath, toteutus(oid, organisaatioOid), parseOid)

  def create(organisaatioOid: OrganisaatioOid, expectedStatus: Int, expectedBody: String): Unit =
    create(ToteutusPath, toteutus(organisaatioOid), defaultSessionId, expectedStatus, expectedBody)

  def create(organisaatioOid: OrganisaatioOid, sessionId: UUID): String =
    create(ToteutusPath, toteutus(organisaatioOid), sessionId, parseOid)

  def update(oid: String, ifUnmodifiedSince: Instant): Unit =
    update(ToteutusPath, toteutus(oid), ifUnmodifiedSince)

  def update(oid: String, ifUnmodifiedSince: Option[Instant], expectedStatus: Int, expectedBody: String): Unit =
    ifUnmodifiedSince match {
      case Some(ifUnmodifiedSinceVal) => update(ToteutusPath, toteutus(oid), ifUnmodifiedSinceVal, defaultSessionId, expectedStatus, expectedBody)
      case _ => update(ToteutusPath, toteutus(oid), defaultSessionId, expectedStatus, expectedBody)
    }

  def update(oid: String, ifUnmodifiedSince: Instant, sessionId: UUID): Unit =
    update(ToteutusPath, toteutus(oid), ifUnmodifiedSince, sessionId)
}
