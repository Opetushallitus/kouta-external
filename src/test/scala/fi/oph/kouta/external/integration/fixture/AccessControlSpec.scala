package fi.oph.kouta.external.integration.fixture

import java.util.UUID

import fi.oph.kouta.external.{MockSecurityContext, OrganisaatioServiceMock}
import fi.oph.kouta.external.database.SessionDAO
import fi.oph.kouta.external.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.security._
import org.scalatra.test.scalatest.ScalatraFlatSpec

import scala.collection.mutable

case class TestUser(oid: String, username: String, sessionId: UUID) {
  val ticket = MockSecurityContext.ticketFor(KoutaIntegrationSpec.serviceIdentifier, username)
}

trait AccessControlSpec extends ScalatraFlatSpec with OrganisaatioServiceMock {
  this: HttpSpec =>

  protected val roleEntities: Seq[RoleEntity] = Seq.empty

  override def beforeAll(): Unit = {
    super.beforeAll()
    startServiceMocking()
    addTestSessions()

    mockOrganisaatioResponses(EvilChildOid, ChildOid, ParentOid, GrandChildOid)
    mockSingleOrganisaatioResponses(LonelyOid)
    mockOrganisaatioResponse(YoOid, responseFromResource("mpkk"))
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopServiceMocking()
  }

  val LonelyOid = OrganisaatioOid("1.2.246.562.10.99999999999")
  val UnknownOid = OrganisaatioOid("1.2.246.562.10.99999999998")
  val YoOid = OrganisaatioOid("1.2.246.562.10.46312206843")

  val crudSessions: mutable.Map[OrganisaatioOid, (UUID, CasSession)] = mutable.Map.empty
  val readSessions: mutable.Map[OrganisaatioOid, (UUID, CasSession)] = mutable.Map.empty

  def crudSessionIds(oid: OrganisaatioOid): UUID = crudSessions(oid)._1
  def readSessionIds(oid: OrganisaatioOid): UUID = readSessions(oid)._1

  var indexerSessionId: UUID = _
  var fakeIndexerSessionId: UUID = _
  var otherRoleSessionId: UUID = _

  def addTestSession(authorities: Seq[Authority]): (UUID, CasSession) = {
    val sessionId = UUID.randomUUID()
    val oid = s"1.2.246.562.24.${math.abs(sessionId.getLeastSignificantBits.toInt)}"
    val user = TestUser(oid, s"user-$oid", sessionId)
    val session = CasSession(ServiceTicket(user.ticket), user.oid, authorities.toSet)
    SessionDAO.store(session, user.sessionId)
    (sessionId, session)
  }

  def addTestSession(role: Role, organisaatioOid: OrganisaatioOid): (UUID, CasSession) =
    addTestSession(Seq(role), organisaatioOid)

  def addTestSession(roles: Seq[Role], organisaatioOid: OrganisaatioOid): (UUID, CasSession) = {
    val authorities = roles.map(Authority(_, organisaatioOid))
    addTestSession(authorities)
  }

  def addTestSessions(): Unit = {
    Seq(ChildOid, EvilChildOid, GrandChildOid, ParentOid, LonelyOid).foreach { org =>
      crudSessions.update(org, addTestSession(roleEntities.map(re => re.Crud.asInstanceOf[Role]), org))
    }

    Seq(ChildOid, YoOid).foreach { org =>
      readSessions.update(org, addTestSession(roleEntities.map(_.Read.asInstanceOf[Role]), org))
    }

    indexerSessionId = addTestSession(Role.Indexer, OphOid)._1
    fakeIndexerSessionId = addTestSession(Role.Indexer, ChildOid)._1
    otherRoleSessionId = addTestSession(Role.UnknownRole("APP_OTHER"), ChildOid)._1
  }
}
