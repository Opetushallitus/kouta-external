package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids.{ChildOid, EvilChildOid, ParentOid}
import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.security.{CasSession, Role}

import java.util.UUID

class HakuSpec
    extends HakuFixture
    with AccessControlSpec
    with GenericGetTests[Haku, HakuOid]
    with GenericCreateTests[Haku]
    with KoutaBackendMock {

  override val roleEntities       = Seq(Role.Haku)
  override val entityPath: String = HakuPath
  override val entityName         = "haku"
  val existingId: HakuOid         = HakuOid("1.2.246.562.29.00000000000000000001")
  val nonExistingId: HakuOid      = HakuOid("1.2.246.562.29.00000000000000000000")
  override val createdOid          = "1.2.246.562.29.123456789"

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      KoutaBackendConverters.convertHaku(haku(organisaatioOid)),
      "kouta-backend.haku",
      responseString,
      session,
      responseStatus
    )

  getTests()

  genericCreateTests()

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
