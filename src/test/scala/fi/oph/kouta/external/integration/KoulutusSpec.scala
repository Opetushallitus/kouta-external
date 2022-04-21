package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{HakuOid, KoulutusOid}
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoulutusFixture}
import fi.oph.kouta.security.Role

import java.util.UUID

class KoulutusSpec extends KoulutusFixture with AccessControlSpec with GenericGetTests[Koulutus, KoulutusOid] with KoutaBackendMock {

  override val roleEntities               = Seq(Role.Koulutus)
  override val getPath: String            = KoulutusPath
  override val entityName                 = "koulutus"
  override val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000001")
  override val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000000")

  val nonExistingSessionId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  val ophKoulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000002")
  val julkinenOid: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000003")
  val tarjoajaOid: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000004")

  val sorakuvausId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  getTests()

  it should "return 401 without a session" in {
    get(nonExistingId, nonExistingSessionId, 401)
  }

  it should "allow the user of proper koulutustyyppi to read koulutus created by oph" in {
    get(ophKoulutusOid, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read koulutus created by oph" in {
    get(ophKoulutusOid, readSessionIds(YoOid), 403)
  }

  it should "allow the user of proper koulutustyyppi to read julkinen koulutus" in {
    get(julkinenOid, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read julkinen koulutus" in {
    get(julkinenOid, readSessionIds(YoOid), 403)
  }

  it should "allow the user of a tarjoaja organization to read the koulutus" in {
    get(tarjoajaOid, readSessionIds(ChildOid))
  }

  it should "allow the user of an ancestor of a tarjoaja organization" in {
    get(tarjoajaOid, crudSessionIds(ParentOid))
  }

  it should "allow the user of a descendant of a tarjoaja organization" in {
    get(tarjoajaOid, crudSessionIds(GrandChildOid))
  }

  "Create koulutus" should "create a koulutus" in {
    mockCreateKoulutus(koulutus(ParentOid), responseStringWithOid("1.2.246.562.13.123456789"))
    create(koulutus("1.2.246.562.13.123456789", ParentOid))
  }

  it should "return the error code and message" in {
    val testError = "{\"error\": \"test error\"}"
    mockCreateKoulutus(koulutus(ChildOid), testError, 400 )

    create(KoulutusPath, koulutus(ChildOid), defaultSessionId, 400, testError)
  }

  it should "include the caller's authentication in the call" in {
    val (sessionId, session) = crudSessions(EvilChildOid)
    mockCreateKoulutus(koulutus(EvilChildOid), responseStringWithOid("1.2.246.562.13.123456789"), 200, Some((sessionId, session)))
    create(koulutus("1.2.246.562.13.123456789", EvilChildOid), sessionId)
  }
}
