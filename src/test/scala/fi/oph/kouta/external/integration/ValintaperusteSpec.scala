package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.TestOids._
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
  override val existingId: UUID    = UUID.fromString("fa7fcb96-3f80-4162-8d19-5b74731cf90c")
  override val nonExistingId: UUID = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  val ophValintaperusteId: UUID = UUID.fromString("171c3d2c-a43e-4155-a68f-f5c9816f3154")
  val julkinenId: UUID          = UUID.fromString("db8acf4f-6e29-409d-93a4-06000fa9a4cd")

  var ophValintaperuste: Valintaperuste = null
  var julkinenValintaperuste: Valintaperuste = null

  getTests()

  it should "allow the user of proper koulutustyyppi to read valintaperuste created by oph" in {
    get(ophValintaperusteId, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read valintaperuste created by oph" in {
    get(ophValintaperusteId, readSessionIds(YoOid), 403)
  }

  it should "allow the user of proper koulutustyyppi to read julkinen valintaperuste" in {
    get(julkinenId, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read julkinen valintaperuste" in {
    get(julkinenId, readSessionIds(YoOid), 403)
  }

}
