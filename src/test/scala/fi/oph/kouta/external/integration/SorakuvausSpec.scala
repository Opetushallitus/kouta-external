package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.TestOids._
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, SorakuvausFixture}
import fi.oph.kouta.security.Role

class SorakuvausSpec extends SorakuvausFixture with AccessControlSpec with GenericGetTests[Sorakuvaus, UUID] {

  override val roleEntities        = Seq(Role.Valintaperuste)
  override val getPath: String     = SorakuvausPath
  override val entityName: String  = "sorakuvaus"
  override val existingId: UUID    = UUID.fromString("03715370-2c2e-40b1-adf9-4de9e4eb3c73")
  override val nonExistingId: UUID = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  val ophSorakuvausId = UUID.fromString("171c3d2c-a43e-4155-a68f-f5c9816f3154")
  val julkinenId      = UUID.fromString("db8acf4f-6e29-409d-93a4-06000fa9a4cd")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockSorakuvaus(existingId, ChildOid)
    addMockSorakuvaus(ophSorakuvausId, OphOid)
    addMockSorakuvaus(julkinenId, LonelyOid, _ + (KoutaFixtureTool.JulkinenKey -> "true"))
  }

  getTests()

  it should "allow the user of proper koulutustyyppi to read sorakuvaus created by oph" in {
    get(ophSorakuvausId, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read sorakuvaus created by oph" in {
    get(ophSorakuvausId, readSessionIds(YoOid), 403)
  }
}
