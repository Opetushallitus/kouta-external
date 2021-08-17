package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.TestOids._
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, ValintaperusteFixture}
import fi.oph.kouta.security.Role

class ValintaperusteSpec
    extends ValintaperusteFixture
    with AccessControlSpec
    with GenericGetTests[Valintaperuste, UUID] {

  override val roleEntities        = Seq(Role.Valintaperuste)
  override val getPath: String     = ValintaperustePath
  override val entityName: String  = "valintaperuste"
  override val existingId: UUID    = UUID.fromString("03715370-2c2e-40b1-adf9-4de9e4eb3c73")
  override val nonExistingId: UUID = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  val ophValintaperusteId: UUID = UUID.fromString("171c3d2c-a43e-4155-a68f-f5c9816f3154")
  val julkinenId: UUID          = UUID.fromString("db8acf4f-6e29-409d-93a4-06000fa9a4cd")

  var ophValintaperuste: Valintaperuste = null
  var julkinenValintaperuste: Valintaperuste = null

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockValintaperuste(existingId, ChildOid)
    ophValintaperuste = addMockValintaperuste(ophValintaperusteId, OphOid)
    julkinenValintaperuste = addMockValintaperuste(julkinenId, LonelyOid, _ + (KoutaFixtureTool.JulkinenKey -> "true"))
  }

  getTests()

  it should "allow the user of proper koulutustyyppi to read valintaperuste created by oph" in {
    get(ophValintaperusteId, readSessionIds(ChildOid), ophValintaperuste)
  }

  it should "deny the user of wrong koulutustyyppi to read valintaperuste created by oph" in {
    get(ophValintaperusteId, readSessionIds(YoOid), 403)
  }

  it should "allow the user of proper koulutustyyppi to read julkinen valintaperuste" in {
    get(julkinenId, readSessionIds(ChildOid), julkinenValintaperuste)
  }

  it should "deny the user of wrong koulutustyyppi to read julkinen valintaperuste" in {
    get(julkinenId, readSessionIds(YoOid), 403)
  }

}
