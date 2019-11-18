package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.domain.{Ataru, EiSähköistä}
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.servlet.HakuServlet
import fi.oph.kouta.external.{OrganisaatioServiceMock, TempElasticClientHolder}

trait HakuFixture extends KoutaIntegrationSpec {
  val HakuPath = "/haku"

  addServlet(new HakuServlet(TempElasticClientHolder), HakuPath)

  def get(oid: HakuOid): Haku = get[Haku](HakuPath, oid)

  def get(oid: HakuOid, sessionId: UUID): Haku = get[Haku](HakuPath, oid, sessionId)

  def get(oid: HakuOid, errorStatus: Int): Unit = get(oid, defaultSessionId, errorStatus)

  def get(oid: HakuOid, sessionId: UUID, errorStatus: Int): Unit = get(s"$HakuPath/$oid", sessionId, errorStatus)

  def addMockHaku(
      hakuOid: HakuOid,
      organisaatioOid: OrganisaatioOid = OrganisaatioServiceMock.ChildOid,
      hakulomakeAtaruId: Option[UUID] = None
  ): Unit = {
    val hakulomakeFields: Map[String, String] = hakulomakeAtaruId match {
      case None =>
        Map(
          KoutaFixtureTool.HakulomaketyyppiKey -> EiSähköistä.toString,
          KoutaFixtureTool.HakulomakeIdKey     -> UUID.randomUUID().toString
        )
      case Some(id) =>
        Map(KoutaFixtureTool.HakulomaketyyppiKey -> Ataru.toString, KoutaFixtureTool.HakulomakeIdKey -> id.toString)
    }

    val haku = KoutaFixtureTool.DefaultHakuScala ++ hakulomakeFields + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addHaku(hakuOid.s, haku)
    indexHaku(hakuOid)
  }
}
