package fi.oph.kouta.external.integration

import java.time.Instant
import java.util.UUID

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.security.Role

class HakuSpec extends HakuFixture with AccessControlSpec with GenericGetTests[Haku, HakuOid] with KoutaBackendMock {

  override val roleEntities = Seq(Role.Haku)
  override val getPath      = HakuPath
  override val entityName   = "haku"
  val existingId            = HakuOid("1.2.246.562.29.00000000000000000009")
  val nonExistingId         = HakuOid("1.2.246.562.29.0")

  val ataruId1 = UUID.randomUUID()
  val ataruId2 = UUID.randomUUID()
  val ataruId3 = UUID.randomUUID()

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockHaku(existingId, ChildOid)

    addMockHaku(HakuOid("1.2.246.562.29.301"), ChildOid, Some(ataruId1))
    addMockHaku(HakuOid("1.2.246.562.29.302"), ChildOid, Some(ataruId1))
    addMockHaku(HakuOid("1.2.246.562.29.303"), ChildOid, Some(ataruId2))
    addMockHaku(HakuOid("1.2.246.562.29.304"), ChildOid, Some(ataruId2))
    addMockHaku(HakuOid("1.2.246.562.29.305"), ParentOid, Some(ataruId1))
    addMockHaku(HakuOid("1.2.246.562.29.306"), EvilChildOid, Some(ataruId1))
  }

  getTests()

  "Create haku" should "create a haku" in {
    mockCreateHaku(haku(ParentOid), "1.2.246.562.29.123456789")

    create(haku("1.2.246.562.29.123456789", ParentOid))
  }

  it should "return the error code and message" in {
    val testError = "{\"error\": \"test error\"}"
    mockCreateHaku(haku(ChildOid), 400, testError)

    create(HakuPath, haku(ChildOid), defaultSessionId, 400, testError)
  }

  it should "include the caller's authentication in the call" in {
    val (sessionId, session) = crudSessions(EvilChildOid)
    mockCreateHaku(haku(EvilChildOid), "1.2.246.562.29.123456789", sessionId, session)
    create(haku("1.2.246.562.29.123456789", EvilChildOid), sessionId)
  }

  "Update haku" should "update a haku" in {
    val now = Instant.now()
    mockUpdateHaku(haku("1.2.246.562.29.1"), now)

    update(haku("1.2.246.562.29.1"), now)
  }

  it should "require x-If-Unmodified-Since header" in {
    update(HakuPath, haku("1.2.246.562.29.1"), defaultSessionId, 400, "{\"error\":\"Otsake x-If-Unmodified-Since on pakollinen.\"}")
  }

  it should "return the error code and message" in {
    val testError = "{\"error\": \"test error\"}"
    val now = Instant.now()

    mockUpdateHaku(haku("1.2.246.562.29.2"), now, 400, testError)
    update(HakuPath, haku("1.2.246.562.29.2"), now, defaultSessionId, 400, testError)
  }

  it should "include the caller's authentication in the call" in {
    val now = Instant.now()
    val (sessionId, session) = crudSessions(EvilChildOid)

    mockUpdateHaku(haku("1.2.246.562.29.3"), now, sessionId, session)
    update(haku("1.2.246.562.29.3"), now, sessionId)
  }
}
