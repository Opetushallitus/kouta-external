package fi.oph.kouta.external.integration.fixture

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{HakukohderyhmaOid, OrganisaatioOid}
import fi.oph.kouta.external.MockSecurityContext
import fi.oph.kouta.external.database.SessionDAO
import fi.oph.kouta.external.servlet.KoutaLightTallentajaRole
import fi.oph.kouta.security._
import org.scalatra.test.scalatest.ScalatraFlatSpec

import java.util.UUID
import scala.collection.mutable

case class TestUser(oid: String, username: String, sessionId: UUID) {
  val ticket: String = MockSecurityContext.ticketFor(KoutaIntegrationSpec.serviceIdentifier, username)
}

trait AccessControlSpec extends ScalatraFlatSpec {
  this: HttpSpec =>

  protected val roleEntities: Seq[RoleEntity] = Seq.empty

  override def beforeAll(): Unit = {
    super.beforeAll()
    addTestSessions()
  }

  val LonelyOid               = OrganisaatioOid("1.2.246.562.10.99999999999")
  val UnknownOid              = OrganisaatioOid("1.2.246.562.10.99999999998")
  val YoOid                   = OrganisaatioOid("1.2.246.562.10.46312206843")
  val hakukohderyhmaOid       = HakukohderyhmaOid("1.2.246.562.28.00000000000000000015")
  val searchHakukohderyhmaOid = HakukohderyhmaOid("1.2.246.562.28.00000000000000000025")
  val KoutaLightOrgOid1       = OrganisaatioOid("1.2.246.562.10.73908278491")
  val KoutaLightOrgOid2       = OrganisaatioOid("1.2.246.562.10.73908278492")

  val crudSessions: mutable.Map[OrganisaatioOid, (UUID, CasSession)]                 = mutable.Map.empty
  val hakukohderyhmaCrudSessions: mutable.Map[HakukohderyhmaOid, (UUID, CasSession)] = mutable.Map.empty
  val readSessions: mutable.Map[OrganisaatioOid, (UUID, CasSession)]                 = mutable.Map.empty

  def crudSessionIds(oid: OrganisaatioOid): UUID                 = crudSessions(oid)._1
  def hakukohderyhmaCrudSessionIds(oid: HakukohderyhmaOid): UUID = hakukohderyhmaCrudSessions(oid)._1
  def readSessionIds(oid: OrganisaatioOid): UUID                 = readSessions(oid)._1

  var ophPaakayttajaSessionId: UUID = _
  var indexerSessionId: UUID        = _
  var fakeIndexerSessionId: UUID    = _
  var otherRoleSessionId: UUID      = _
  var hakukohderyhmaSessionId: UUID = _
  var koutaLightSessionId: UUID     = _
  var faultyKoutaLightSessionId: UUID    = _

  def addTestSession(authorities: Seq[Authority]): (UUID, CasSession) = {
    val sessionId = UUID.randomUUID()
    val oid       = s"1.2.246.562.24.${math.abs(sessionId.getLeastSignificantBits.toInt)}"
    val user      = TestUser(oid, s"user-$oid", sessionId)
    val session   = CasSession(ServiceTicket(user.ticket), user.oid, authorities.toSet)
    SessionDAO.store(session, user.sessionId)
    (sessionId, session)
  }

  def addTestSession(role: Role, organisaatioOid: OrganisaatioOid): (UUID, CasSession) =
    addTestSession(Seq(role), organisaatioOid)

  def addTestSession(role: Role, hakukohderyhmaOid: HakukohderyhmaOid): (UUID, CasSession) =
    addTestSession(Seq(role), hakukohderyhmaOid)

  def addTestSession(roles: Seq[Role], organisaatioOid: OrganisaatioOid): (UUID, CasSession) = {
    val authorities = roles.map(Authority(_, organisaatioOid))
    addTestSession(authorities)
  }

  def addTestSessionWithMultipleOrgs(roles: Seq[(Role, OrganisaatioOid)]): (UUID, CasSession) = {
    val authorities = roles.map(role => Authority(role._1, role._2))
    addTestSession(authorities)
  }

  def addTestSession(roles: Seq[Role], hakukohderyhmaOid: HakukohderyhmaOid): (UUID, CasSession) = {
    val authorities = roles.map(Authority(_, hakukohderyhmaOid))
    addTestSession(authorities)
  }

  def addTestSessions(): Unit = {
    Seq(ChildOid, EvilChildOid, GrandChildOid, ParentOid, LonelyOid).foreach { org =>
      crudSessions.update(org, addTestSession(roleEntities.map(re => re.Crud.asInstanceOf[Role]), org))
    }

    Seq(ChildOid, YoOid).foreach { org =>
      readSessions.update(org, addTestSession(roleEntities.map(_.Read.asInstanceOf[Role]), org))
    }

    Seq(hakukohderyhmaOid, searchHakukohderyhmaOid).foreach { h =>
      hakukohderyhmaCrudSessions.update(h, addTestSession(roleEntities.map(_.Read.asInstanceOf[Role]), h))
    }

    ophPaakayttajaSessionId = addTestSession(Role.Paakayttaja, OphOid)._1
    indexerSessionId = addTestSession(Role.Indexer, OphOid)._1
    fakeIndexerSessionId = addTestSession(Role.Indexer, ChildOid)._1
    otherRoleSessionId = addTestSession(Role.UnknownRole("APP_OTHER"), ChildOid)._1
    hakukohderyhmaSessionId = addTestSession(Role.UnknownRole("APP_KOUTA_HAKUKOHDE"), hakukohderyhmaOid)._1
    koutaLightSessionId = addTestSession(KoutaLightTallentajaRole, KoutaLightOrgOid1)._1
    faultyKoutaLightSessionId = addTestSessionWithMultipleOrgs(
      List((KoutaLightTallentajaRole, KoutaLightOrgOid1), (KoutaLightTallentajaRole, KoutaLightOrgOid2))
    )._1
  }
}
