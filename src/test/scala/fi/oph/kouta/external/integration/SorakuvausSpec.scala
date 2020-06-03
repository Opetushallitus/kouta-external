package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.TestOids._
import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, SorakuvausFixture}
import fi.oph.kouta.security.Role

class SorakuvausSpec extends SorakuvausFixture with AccessControlSpec with GenericGetTests[Sorakuvaus, UUID] {

  override val roleEntities        = Seq(Role.Valintaperuste)
  override val getPath: String     = SorakuvausPath
  override val entityName: String  = "sorakuvaus"
  override val existingId: UUID    = UUID.fromString("03715370-2c2e-40b1-adf9-4de9e4eb3c73")
  override val nonExistingId: UUID = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockSorakuvaus(existingId, ChildOid)
  }

  getTests()
}
