package fi.oph.kouta.external.integration

import java.util.UUID
import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, SorakuvausFixture}
import fi.oph.kouta.security.{CasSession, Role}

import java.time.Instant

class SorakuvausSpec
    extends SorakuvausFixture
    with AccessControlSpec
    with GenericCreateTests[Sorakuvaus]
    with GenericUpdateTests[Sorakuvaus]
    with KoutaBackendMock {

  override val roleEntities = Seq(Role.Valintaperuste)
  val existingId: UUID      = UUID.fromString("e17773b2-f5a0-418d-a49f-34578c4b3625")
  val nonExistingId: UUID   = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  val entityName             = "sorakuvaus"
  override val createdId     = "e17773b2-f5a0-418d-a49f-34578c4b3625"
  override val updatedIdBase = "e17773b2-f5a0-418d-a49f-34578c4b362"

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      KoutaBackendConverters.convertSorakuvaus(sorakuvaus(organisaatioOid)),
      "kouta-backend.sorakuvaus",
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
      KoutaBackendConverters.convertSorakuvaus(sorakuvaus(oidOrId)),
      "kouta-backend.sorakuvaus",
      ifUnmodifiedSince,
      session,
      responseString,
      responseStatus
    )

  val ophSorakuvausId = UUID.fromString("171c3d2c-a43e-4155-a68f-f5c9816f3154")

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

  genericCreateTests()

  genericUpdateTests()
}
