package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.external.client.OrganisaatioServiceImpl
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.service.ToteutusService
import fi.oph.kouta.external.servlet.ToteutusServlet
import fi.oph.kouta.external.{KoutaFixtureTool, TempElasticClient}

trait ToteutusFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val ToteutusPath = "/toteutus"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val toteutusService = new ToteutusService(new ToteutusClient(TempElasticClient.client), organisaatioService)
    addServlet(new ToteutusServlet(toteutusService), ToteutusPath)
  }

  def get(oid: ToteutusOid): Toteutus = get[Toteutus](ToteutusPath, oid)

  def get(oid: ToteutusOid, sessionId: UUID): Toteutus = get[Toteutus](ToteutusPath, oid, sessionId)

  def get(oid: ToteutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$ToteutusPath/$oid", sessionId, errorStatus)

  def addMockToteutus(
      toteutusOid: ToteutusOid,
      organisaatioOid: OrganisaatioOid,
      koulutusOid: KoulutusOid,
      modifier: Map[String, String] => Map[String, String] = identity
  ): Unit = {
    val toteutus = KoutaFixtureTool.DefaultToteutusScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s) +
      (KoutaFixtureTool.KoulutusOidKey  -> koulutusOid.s)
    KoutaFixtureTool.addToteutus(toteutusOid.s, modifier(toteutus))
    indexToteutus(toteutusOid)
  }
}
