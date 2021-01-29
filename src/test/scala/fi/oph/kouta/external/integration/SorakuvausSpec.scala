package fi.oph.kouta.external.integration

import java.util.UUID
import fi.oph.kouta.TestOids._
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, SorakuvausFixture}
import fi.oph.kouta.security.Role

class SorakuvausSpec extends SorakuvausFixture with AccessControlSpec {

  override val roleEntities = Seq(Role.Valintaperuste)
  val existingId: UUID = UUID.fromString("03715370-2c2e-40b1-adf9-4de9e4eb3c73")
  val nonExistingId: UUID = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  val ophSorakuvausId = UUID.fromString("171c3d2c-a43e-4155-a68f-f5c9816f3154")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockSorakuvaus(existingId, ChildOid)
    addMockSorakuvaus(ophSorakuvausId, OphOid)
  }

  s"GET $SorakuvausPath/:id" should s"get sorakuvaus from elastic search" in {
    get(existingId, defaultSessionId)
  }

  it should s"have ${KoutaServlet.LastModifiedHeader} header in the response" in {
    get(s"$SorakuvausPath/$existingId", headers = Seq(defaultSessionHeader)) {
      status should equal(200)
      header.get(KoutaServlet.LastModifiedHeader) should not be empty
      KoutaServlet.parseHttpDate(header(KoutaServlet.LastModifiedHeader)).toOption should not be empty
    }
  }

  it should s"return 404 if sorakuvaus not found" in {
    get(s"$SorakuvausPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id $nonExistingId from sorakuvaus-kouta")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$SorakuvausPath/$nonExistingId") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should s"allow a user of the non oph organization to read the sorakuvaus 1" in {
    get(existingId, crudSessionIds(ChildOid))
  }

  it should s"allow a user of the non oph organization to read the sorakuvaus 2" in {
    get(existingId, crudSessionIds(LonelyOid))
  }

  it should s"allow a user of the non oph organization to read the sorakuvaus 3" in {
    get(existingId, crudSessionIds(ParentOid))
  }

  it should s"allow a user of the non oph organization to read the sorakuvaus 4" in {
    get(existingId, crudSessionIds(GrandChildOid))
  }

  it should "deny a user with the wrong role" in {
    get(existingId, otherRoleSessionId, 403)
  }

  it should "deny indexer access" in {
    get(existingId, indexerSessionId, 403)
  }
  it should "allow the user of proper koulutustyyppi to read sorakuvaus created by oph" in {
    get(ophSorakuvausId, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read sorakuvaus created by oph" in {
    get(ophSorakuvausId, readSessionIds(YoOid), 403)
  }
}
