package fi.oph.kouta.external.integration.fixture

import java.time.Instant
import java.util.UUID
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.{KoutaConfigurationFactory, TestSetups}
import fi.oph.kouta.external.database.SessionDAO
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.mocks.{OrganisaatioServiceMock, SpecWithMocks, UrlProperties}
import fi.oph.kouta.security.{Authority, CasSession, RoleEntity, ServiceTicket}
import fi.oph.kouta.util.TimeUtils
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.{JBool, JObject}
import org.scalactic.Equality
import org.scalatra.test.scalatest.ScalatraFlatSpec
import slick.jdbc.GetResult

trait KoutaIntegrationSpec extends ScalatraFlatSpec
  with SpecWithMocks
  with UrlProperties
  with HttpSpec
  with OrganisaatioServiceMock
  with DatabaseSpec
  with ElasticDumpFixture {

  System.setProperty("kouta-backend.useSecureCookies", "false")
  KoutaConfigurationFactory.setupWithDefaultTemplateFile()
  setUrlProperties(KoutaConfigurationFactory.configuration.urlProperties)
  TestSetups.setupPostgres()

  val serviceIdentifier  = KoutaIntegrationSpec.serviceIdentifier
  val rootOrganisaatio   = KoutaIntegrationSpec.rootOrganisaatio
  val defaultAuthorities = KoutaIntegrationSpec.defaultAuthorities

  val testUser = TestUser("test-user-oid", "testuser", defaultSessionId)

  def addDefaultSession(): Unit = {
    SessionDAO.store(CasSession(ServiceTicket(testUser.ticket), testUser.oid, defaultAuthorities), testUser.sessionId)
  }

  def responseStringWithOid(oid: String): String =
    s"""{"oid": "$oid"}"""
  def responseStringWithId(id: String): String =
    s"""{"id": "$id"}"""

  override def beforeAll(): Unit = {
    super.beforeAll()
    addDefaultSession()
    initIndices()
    mockOrganisaatioResponse()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    truncateDatabase()
  }
}

object KoutaIntegrationSpec {
  val serviceIdentifier = "testService"

  val rootOrganisaatio: OrganisaatioOid  = OrganisaatioOid("1.2.246.562.10.00000000001")
  val defaultAuthorities: Set[Authority] = RoleEntity.all.map(re => Authority(re.Crud, rootOrganisaatio)).toSet
}

sealed trait HttpSpec extends KoutaJsonFormats { this: ScalatraFlatSpec =>
  val defaultSessionId = UUID.randomUUID()

  val DebugJson = false

  def debugJson[E <: AnyRef](body: String, url: String)(implicit mf: Manifest[E]): Unit = {
    if (DebugJson) {
      import org.json4s.jackson.Serialization.writePretty
      println(s"Response for GET $url")
      println(writePretty[E](read[E](body)))
    }
  }

  def jsonHeader = "Content-Type" -> "application/json; charset=utf-8"
  val IfUnmodifiedSinceHeader: String = "x-If-Unmodified-Since"
  val LastModifiedHeader: String = "x-Last-Modified"


  def ifUnmodifiedSinceHeader(lastModified: Instant): (String, String) =
    IfUnmodifiedSinceHeader -> TimeUtils.renderHttpDate(lastModified)

  def sessionHeader(sessionId: String): (String, String) = "Cookie" -> s"session=$sessionId"
  def sessionHeader(sessionId: UUID): (String, String)   = sessionHeader(sessionId.toString)

  def defaultSessionHeader: (String, String) = sessionHeader(defaultSessionId)

  def defaultHeaders: Seq[(String, String)] = Seq(defaultSessionHeader, jsonHeader)

  def bytes(o: AnyRef) = write(o).getBytes

  val parseOid = (body: String) => read[Oid](body).oid
  val parseId = (body: String) => read[Uuid](body).id

  def get[E <: scala.AnyRef](path: String, id: Object)(
      implicit equality: Equality[E],
      mf: Manifest[E]
  ): E =
    get(path, id, defaultSessionId)

  def get[E <: scala.AnyRef](path: String, id: Object, sessionId: UUID)(
      implicit equality: Equality[E],
      mf: Manifest[E]
  ): E =
    get(s"$path/${id.toString}", sessionId)

  def getObject[E <: scala.AnyRef](path: String)(
      implicit equality: Equality[E],
      mf: Manifest[E]
  ): E = get(path, defaultSessionId)

  def get[E <: scala.AnyRef](path: String, sessionId: UUID)(
      implicit equality: Equality[E],
      mf: Manifest[E]
  ): E = get(path, headers = Seq(sessionHeader(sessionId))) {
    withClue(body) {
      status should equal(200)
    }
    debugJson(body, path)
    read[E](body)
  }

  def get(path: String, sessionId: UUID, expectedStatus: Int): Unit = {
    get(path, headers = Seq(sessionHeader(sessionId))) {
      withClue(body) {
        status should equal(expectedStatus)
      }
    }
  }

  def get[E <: scala.AnyRef, I](path: String, id: I, sessionId: UUID, expected: E)(implicit equality: Equality[E], mf: Manifest[E]): String = {
    get(s"$path/${id.toString}", headers = Seq(sessionHeader(sessionId))) {
      withClue(body) {
        status should equal(200)
      }
      debugJson(body, s"$path/${id.toString}")
      read[E](body) should equal(expected)
      header(LastModifiedHeader)
    }
  }


  def create[E <: scala.AnyRef, R](path: String, entity: E, sessionId: UUID, result: String => R): R = {
    put(path, bytes(entity), headers = Seq(sessionHeader(sessionId))) {
      withClue(body) {
        status should equal(200)
      }
      result(body)
    }
  }

  def create[E <: scala.AnyRef, R](path: String, entity: E, result: String => R): R =
    create(path, entity, defaultSessionId, result)

  def create[E <: scala.AnyRef](path: String, entity: E, sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    put(path, bytes(entity), headers = Seq(sessionHeader(sessionId))) {
      withClue(body) {
        status should equal(expectedStatus)
        body should equal(expectedBody)
      }
    }

  def update[E <: scala.AnyRef](path: String, entity: E, ifUnmodifiedSince: Instant, sessionId: UUID): Unit = {
    post(path, bytes(entity), headers = Seq(sessionHeader(sessionId), ifUnmodifiedSinceHeader(ifUnmodifiedSince))) {
      withClue(body) {
        status should equal(200)
        (parse(body).asInstanceOf[JObject] \\ "updated").asInstanceOf[JBool].value shouldEqual true
      }
    }
  }

  def update[E <: scala.AnyRef](path: String, entity: E, ifUnmodifiedSince: Instant): Unit =
    update(path, entity, ifUnmodifiedSince, defaultSessionId)

  def update[E <: scala.AnyRef](path: String, entity: E, headers: Seq[(String, String)], expectedStatus: Int, expectedBody: String): Unit =
    post(path, bytes(entity), headers = headers) {
      withClue(body) {
        status should equal(expectedStatus)
        body should equal(expectedBody)
      }
    }

  def update[E <: scala.AnyRef](path: String, entity: E, sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    update(path, entity, Seq(sessionHeader(sessionId)), expectedStatus, expectedBody)

  def update[E <: scala.AnyRef](path: String, entity: E, ifUnmodifiedSince: Instant, sessionId: UUID, expectedStatus: Int, expectedBody: String): Unit =
    update(path, entity, Seq(sessionHeader(sessionId), ifUnmodifiedSinceHeader(ifUnmodifiedSince)), expectedStatus, expectedBody)

}

sealed trait DatabaseSpec {
  KoutaConfigurationFactory.setupWithDefaultTemplateFile()
  TestSetups.setupPostgres()
  import fi.oph.kouta.external.database.KoutaDatabase

  private lazy val db = KoutaDatabase

  def truncateDatabase() = {
    import slick.jdbc.PostgresProfile.api._

    db.runBlocking(sqlu"""delete from authorities""")
    db.runBlocking(sqlu"""delete from sessions""")
  }

  import java.time._

  implicit val getInstant: AnyRef with GetResult[LocalDateTime] = slick.jdbc.GetResult[LocalDateTime](
    r => LocalDateTime.ofInstant(r.nextTimestamp().toInstant, ZoneId.of("Europe/Helsinki")).withNano(0).withSecond(0)
  )
}
