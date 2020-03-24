package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.external.{KoutaFixtureTool, TempElasticClient}
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.domain.oid.{KoulutusOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.external.elasticsearch.ToteutusClient
import fi.oph.kouta.external.service.ToteutusService
import fi.oph.kouta.external.servlet.ToteutusServlet

trait ToteutusFixture extends KoutaIntegrationSpec {
  val ToteutusPath = "/toteutus"

  addServlet(new ToteutusServlet(new ToteutusService(new ToteutusClient(TempElasticClient.client))), ToteutusPath)

  def get(oid: ToteutusOid): Toteutus = get[Toteutus](ToteutusPath, oid)

  def get(oid: ToteutusOid, sessionId: UUID): Toteutus = get[Toteutus](ToteutusPath, oid, sessionId)

  def get(oid: ToteutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$ToteutusPath/$oid", sessionId, errorStatus)

  def addMockToteutus(toteutusOid: ToteutusOid, organisaatioOid: OrganisaatioOid, koulutusOid: KoulutusOid): Unit = {
    val toteutus = KoutaFixtureTool.DefaultToteutusScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s) +
      (KoutaFixtureTool.KoulutusOidKey  -> koulutusOid.s)
    KoutaFixtureTool.addToteutus(toteutusOid.s, toteutus)
    indexToteutus(toteutusOid)
  }
}
