package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids.{ChildOid, EvilChildOid, ParentOid}
import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.security.Role

class HakuSpec extends HakuFixture with AccessControlSpec with GenericGetTests[Haku, HakuOid] with KoutaBackendMock {

  override val roleEntities = Seq(Role.Haku)
  override val getPath: String = HakuPath
  override val entityName   = "haku"
  val existingId: HakuOid = HakuOid("1.2.246.562.29.00000000000000000001")
  val nonExistingId: HakuOid = HakuOid("1.2.246.562.29.00000000000000000000")

  getTests()

  "Create haku" should "create a haku" in {
    mockCreateHaku(haku(ParentOid), responseStringWithOid("1.2.246.562.29.123456789"))
    create(haku("1.2.246.562.29.123456789", ParentOid))
  }

  it should "return the error code and message" in {
    val testError = "{\"error\": \"test error\"}"
    mockCreateHaku(haku(ChildOid), testError, 400)

    create(HakuPath, haku(ChildOid), defaultSessionId, 400, testError)
  }

  it should "include the caller's authentication in the call" in {
    val (sessionId, session) = crudSessions(EvilChildOid)
    mockCreateHaku(haku(EvilChildOid), responseStringWithOid("1.2.246.562.29.123456789"), 200, Some((sessionId, session)))
    create(haku("1.2.246.562.29.123456789", EvilChildOid), sessionId)
  }

//  "Update haku" should "update a haku" in {
//    val now = Instant.now()
//    mockUpdateHaku(haku("1.2.246.562.29.1"), now)
//
//    update(haku("1.2.246.562.29.1"), now)
//  }

//  it should "require x-If-Unmodified-Since header" in {
//    update(HakuPath, haku("1.2.246.562.29.1"), defaultSessionId, 400, "{\"error\":\"Otsake x-If-Unmodified-Since on pakollinen.\"}")
//  }

//  it should "return the error code and message" in {
//    val testError = "{\"error\": \"test error\"}"
//    val now = Instant.now()
//
//    mockUpdateHaku(haku("1.2.246.562.29.2"), now, 400, testError)
//    update(HakuPath, haku("1.2.246.562.29.2"), now, defaultSessionId, 400, testError)
//  }

//  it should "include the caller's authentication in the call" in {
//    val now = Instant.now()
//    val (sessionId, session) = crudSessions(EvilChildOid)
//
//    mockUpdateHaku(haku("1.2.246.562.29.3"), now, sessionId, session)
//    update(haku("1.2.246.562.29.3"), now, sessionId)
//  }
}
