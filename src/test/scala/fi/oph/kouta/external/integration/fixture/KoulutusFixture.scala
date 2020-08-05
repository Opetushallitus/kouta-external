package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.oid.{KoulutusOid, OrganisaatioOid}
import fi.oph.kouta.external.servlet.KoulutusServlet
import fi.oph.kouta.external.{OrganisaatioServiceMock, TempElasticClientHolder}

trait KoulutusFixture extends KoutaIntegrationSpec {
  val KoulutusPath = "/koulutus"

  addServlet(new KoulutusServlet(TempElasticClientHolder), KoulutusPath)

  def get(oid: KoulutusOid): Koulutus = get[Koulutus](KoulutusPath, oid)

  def get(oid: KoulutusOid, sessionId: UUID): Koulutus = get[Koulutus](KoulutusPath, oid, sessionId)

  def get(oid: KoulutusOid, sessionId: UUID, errorStatus: Int): Unit = get(s"$KoulutusPath/$oid", sessionId, errorStatus)

  def addMockKoulutus(koulutusOid: KoulutusOid, organisaatioOid: OrganisaatioOid = OrganisaatioServiceMock.ChildOid): Unit = {
    val koulutus = KoutaFixtureTool.DefaultKoulutusScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addKoulutus(koulutusOid.s, koulutus)
    indexKoulutus(koulutusOid)
  }
}
