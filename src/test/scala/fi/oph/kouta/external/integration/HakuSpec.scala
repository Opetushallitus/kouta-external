package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.external.security.Role

class HakuSpec extends HakuFixture with AccessControlSpec with GenericGetTests[Haku, HakuOid] {

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

  "Search by Ataru ID" should "find haku based on Ataru ID" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1", defaultSessionId)

    val ataruIds = haut.map(_.hakulomakeAtaruId)
    ataruIds.foreach(_ should not be empty)
    ataruIds.map(_.get).foreach(_ shouldEqual ataruId1)

    haut.map(_.oid.get) should contain theSameElementsAs Seq(
      HakuOid("1.2.246.562.29.301"),
      HakuOid("1.2.246.562.29.302"),
      HakuOid("1.2.246.562.29.305"),
      HakuOid("1.2.246.562.29.306")
    )
  }

  it should "return 404 if no haut are found" in {
    get(s"$HakuPath/search?ataruId=$ataruId3", defaultSessionId, 404)
  }

  it should "return 401 without a valid session" in {
    get(s"$HakuPath/search?ataruId=$ataruId3") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should s"list the haut the user has access to" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1", crudSessions(ChildOid))

    haut.map(_.oid.get) should contain theSameElementsAs Seq(
      HakuOid("1.2.246.562.29.301"),
      HakuOid("1.2.246.562.29.302")
    )
  }

  it should s"return 404 if the user doesn't have access to any of the matching haku" in {
    get(s"$HakuPath/search?ataruId=$ataruId1", crudSessions(LonelyOid), 404)
  }

  it should s"allow a user of an ancestor organization to get the haut" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1", crudSessions(ParentOid))

    haut.map(_.oid.get) should contain theSameElementsAs Seq(
      HakuOid("1.2.246.562.29.301"),
      HakuOid("1.2.246.562.29.302"),
      HakuOid("1.2.246.562.29.305"),
      HakuOid("1.2.246.562.29.306")
    )
  }

  it should "deny a user with only access to a descendant organization" in {
    get(s"$HakuPath/search?ataruId=$ataruId1", crudSessions(GrandChildOid), 404)
  }

  it should "deny a user with the wrong role" in {
    get(s"$HakuPath/search?ataruId=$ataruId1", otherRoleSession, 403)
  }

  it should "deny indexer access" in {
    get(s"$HakuPath/search?ataruId=$ataruId1", indexerSession, 403)
  }
}
