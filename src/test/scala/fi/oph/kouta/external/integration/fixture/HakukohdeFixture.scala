package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.TestData.JulkaistuHakukohde
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.{HakuClient, HakukohdeClient}
import fi.oph.kouta.external.service.{HakuService, HakukohdeService, HakukohderyhmaService, OrganisaatioServiceImpl}
import fi.oph.kouta.external.servlet.HakukohdeServlet
import fi.oph.kouta.external.{MockHakukohderyhmaClient, MockKoutaClient, TempElasticClient}

import java.util.UUID

trait HakukohdeFixture extends KoutaIntegrationSpec with AccessControlSpec {
  val HakukohdePath = "/hakukohde"
  val HakukohdeSearchPath = "/hakukohde/search"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val organisaatioService = new OrganisaatioServiceImpl(urlProperties.get)
    val hakukohderyhmaClient = new MockHakukohderyhmaClient(urlProperties.get)

    val hakuService = new HakuService(new HakuClient(TempElasticClient.client), new MockKoutaClient(urlProperties.get), organisaatioService)
    val hakukohderyhmaService = new HakukohderyhmaService(hakukohderyhmaClient, organisaatioService)
    val hakukohdeService = new HakukohdeService(new HakukohdeClient(TempElasticClient.client),hakukohderyhmaService, new MockKoutaClient(urlProperties.get), organisaatioService, hakuService)
    addServlet(new HakukohdeServlet(hakukohdeService), HakukohdePath)
  }

  val hakukohde = JulkaistuHakukohde

  private def parseSearchPath(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String], all: Boolean): String = {
    val hakuString: String = hakuOid match {
      case Some(s) => s"haku=${s.toString}"
      case None => ""
    }

    val tarjoajaString: String = tarjoajaOids match {
      case Some(oids) =>
        val tarjoajat: String = oids.map(s => s"&tarjoaja=${s.toString}").toString()
        if (hakuString.nonEmpty) tarjoajat.substring(1) else tarjoajat
      case None => ""
    }

    val queryString: String = q match {
      case Some(s) => if (hakuString.nonEmpty || tarjoajaString.nonEmpty) s"&q=$s&" else s"q=$s&"
      case None => ""
    }

    val allString: String = if (hakuString.nonEmpty || tarjoajaString.nonEmpty || queryString.nonEmpty) s"&all=${all.toString}" else s"all=${all.toString}"

    logger.info(s"$HakukohdeSearchPath?$hakuString$tarjoajaString$queryString$allString")

    s"$HakukohdeSearchPath?$hakuString$tarjoajaString$queryString$allString"
  }

  def hakukohde(organisaatioOid: OrganisaatioOid): Hakukohde =
    hakukohde.copy(organisaatioOid = organisaatioOid)

  def hakukohde(oid: String, organisaatioOid: OrganisaatioOid): Hakukohde =
    hakukohde.copy(oid = Some(HakukohdeOid(oid)), organisaatioOid = organisaatioOid)

  def get(oid: HakukohdeOid): Hakukohde = get[Hakukohde](HakukohdePath, oid)

  def get(oid: HakukohdeOid, sessionId: UUID): Hakukohde = get[Hakukohde](HakukohdePath, oid, sessionId)

  def get(oid: HakukohdeOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$HakukohdePath/$oid", sessionId, errorStatus)

  def create(oid: String, organisaatioOid: OrganisaatioOid): String =
    create(HakukohdePath, hakukohde(oid, organisaatioOid), parseOid)

  def create(organisaatioOid: OrganisaatioOid, expectedStatus: Int, expectedBody: String): Unit =
    create(HakukohdePath, hakukohde(organisaatioOid), defaultSessionId, expectedStatus, expectedBody)

  def create(organisaatioOid: OrganisaatioOid, sessionId: UUID): String =
    create(HakukohdePath, hakukohde(organisaatioOid), sessionId, parseOid)

  def search(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String], all: Boolean, sessionId: UUID): Seq[Hakukohde] = {
    val searchPath: String = parseSearchPath(hakuOid, tarjoajaOids, q, all)
    get[Seq[Hakukohde]](searchPath, sessionId)
  }

  def search(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String], all: Boolean, sessionId: UUID, errorStatus: Int): Unit = {
    val searchPath: String = parseSearchPath(hakuOid, tarjoajaOids, q, all)
    get(searchPath, sessionId, errorStatus)
  }
}
