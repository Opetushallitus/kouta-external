package fi.oph.kouta.external.integration.fixture

import java.time.Instant
import java.util.UUID

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.domain.{Ataru, EiSähköistä, Julkaisutila}
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
    val koutaClient = new MockKoutaClient(urlProperties.get)
    val hakuService = new HakuService(new HakuClient(TempElasticDockerClient.client), koutaClient, organisaatioService)
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

  def create(haku: Haku): String =
    create(HakuPath, haku, parseOid)

  def create(haku: Haku, sessionId: UUID): String =
    create(HakuPath, haku, sessionId, parseOid)

  def update(haku: Haku, ifUnmodifiedSince: Instant): Unit =
    update(HakuPath, haku, ifUnmodifiedSince)

  def update(haku: Haku, ifUnmodifiedSince: Instant, sessionId: UUID): Unit =
    update(HakuPath, haku, ifUnmodifiedSince, sessionId)

  def addMockHaku(
                   hakuOid: HakuOid,
                   organisaatioOid: OrganisaatioOid = ChildOid,
                   hakulomakeAtaruId: Option[UUID] = None
                 ): Unit = {
    val hakulomakeFields: Map[String, String] = hakulomakeAtaruId match {
      case None =>
        Map(
          KoutaFixtureTool.HakulomaketyyppiKey -> EiSähköistä.toString,
          KoutaFixtureTool.HakulomakeIdKey -> UUID.randomUUID().toString
        )
      case Some(id) =>
        Map(KoutaFixtureTool.HakulomaketyyppiKey -> Ataru.toString, KoutaFixtureTool.HakulomakeIdKey -> id.toString)
    }

    val haku = KoutaFixtureTool.DefaultHakuScala ++ hakulomakeFields + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addHaku(hakuOid.s, haku)
    indexHaku(hakuOid)
  }
}
