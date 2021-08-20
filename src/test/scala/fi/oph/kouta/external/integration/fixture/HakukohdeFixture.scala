package fi.oph.kouta.external.integration.fixture

import java.util.UUID
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.{HakuClient, HakukohdeClient}
import fi.oph.kouta.external.service.{HakuService, HakukohdeService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.HakukohdeServlet
import fi.oph.kouta.external.{KoutaFixtureTool, MockKoutaClient, TempElasticDockerClient}

trait HakukohdeFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val HakukohdePath = "/hakukohde"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val koutaClient = new MockKoutaClient(urlProperties.get)

    val hakuService = new HakuService(new HakuClient(TempElasticDockerClient.client), koutaClient, organisaatioService)
    val hakukohdeService = new HakukohdeService(new HakukohdeClient(TempElasticDockerClient.client), organisaatioService, hakuService)
    addServlet(new HakukohdeServlet(hakukohdeService), HakukohdePath)
  }

  def get(oid: HakukohdeOid): Hakukohde = get[Hakukohde](HakukohdePath, oid)

  def get(oid: HakukohdeOid, sessionId: UUID): Hakukohde = get[Hakukohde](HakukohdePath, oid, sessionId)

  def get(oid: HakukohdeOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$HakukohdePath/$oid", sessionId, errorStatus)

  def addMockHakukohde(
      hakukohdeOid: HakukohdeOid,
      organisaatioOid: OrganisaatioOid,
      hakuOid: HakuOid,
      toteutusOid: ToteutusOid,
      valintaperusteId: UUID
  ): Unit = {
    val hakukohde = KoutaFixtureTool.DefaultHakukohdeScala +
      (KoutaFixtureTool.OrganisaatioKey     -> organisaatioOid.s) +
      (KoutaFixtureTool.HakuOidKey          -> hakuOid.s) +
      (KoutaFixtureTool.ToteutusOidKey      -> toteutusOid.s) +
      (KoutaFixtureTool.ValintaperusteIdKey -> valintaperusteId.toString)
    KoutaFixtureTool.addHakukohde(hakukohdeOid.s, hakukohde)
    indexHakukohde(hakukohdeOid)
  }
}
