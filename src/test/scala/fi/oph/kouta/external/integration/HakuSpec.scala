package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.security.{CasSession, Role}

import java.time.Instant
import java.util.UUID

class HakuSpec
    extends HakuFixture
    with AccessControlSpec
    with GenericGetTests[Haku, HakuOid]
    with GenericCreateTests[Haku]
    with GenericUpdateTests[Haku]
    with KoutaBackendMock {

  override val roleEntities       = Seq(Role.Haku)
  override val entityPath: String = HakuPath
  override val entityName         = "haku"
  val existingId: HakuOid         = HakuOid("1.2.246.562.29.00000000000000000001")
  val nonExistingId: HakuOid      = HakuOid("1.2.246.562.29.00000000000000000000")
  override val createdOid         = "1.2.246.562.29.123456789"
  override val updatedOidBase     = "1.2.246.562.29.1"

  val ophOid   = "1.2.246.562.10.00000000001"
  val hakuOid2 = HakuOid("1.2.246.562.29.00000000000000000002")
  val hakuOid3 = HakuOid("1.2.246.562.29.00000000000000000003")
  val hakuOid4 = HakuOid("1.2.246.562.29.00000000000000000004")
  val hakuOid5 = HakuOid("1.2.246.562.29.00000000000000000005")
  val hakuOid6 = HakuOid("1.2.246.562.29.00000000000000000006")

  val ataruId1: UUID = UUID.fromString("dcd38a87-912e-4e91-8840-99c7e242dd53")
  val ataruId2: UUID = UUID.fromString("dcd38a87-912e-4e91-8840-99c7e242dd54")
  val ataruId3: UUID = UUID.fromString("dcd38a87-912e-4e91-8840-99c7e242dd55")

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      entityName,
      haku(organisaatioOid),
      "kouta-backend.haku",
      responseString,
      session,
      responseStatus
    )

  def mockUpdate(
      oidOrId: String,
      ifUnmodifiedSince: Option[Instant],
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addUpdateMock(
      entityName,
      haku(oidOrId),
      "kouta-backend.haku",
      ifUnmodifiedSince,
      session,
      responseString,
      responseStatus
    )

  getTests()

  genericCreateTests()

  genericUpdateTests()

  "Search by Ataru ID" should "find haku based on Ataru ID" in {
    val haut =
      get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1&tarjoaja=1.2.246.562.10.00000000001", defaultSessionId)

    val ataruIds = haut.map(_.hakulomakeAtaruId)
    ataruIds.foreach(_ should not be empty)
    ataruIds.map(_.get).foreach(_ shouldEqual ataruId1)

    println(s"haut: ${haut.map(_.oid)}")

    haut.map(_.oid.get) should contain theSameElementsAs Seq(
      existingId,
      hakuOid2,
      hakuOid5,
      hakuOid6
    )
  }

  it should "return 200 if no haut are found" in {
    get(
      path = s"$HakuPath/search?ataruId=$ataruId3&tarjoaja=1.2.246.562.10.00000000001",
      sessionId = defaultSessionId,
      200
    )
  }

  it should "return 400 without tarjoaja" in {
    get(s"$HakuPath/search?ataruId=$ataruId3", defaultSessionId, 400)
  }
}
