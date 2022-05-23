package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoutaIntegrationSpec}
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.TestOids._

trait GenericGetTests[E, ID] {
  this: KoutaIntegrationSpec with AccessControlSpec =>

  def entityPath: String
  def entityName: String
  def index: String = s"$entityName-kouta"
  def existingId: ID
  def nonExistingId: ID

  def get(id: ID, sessionId: UUID): E
  def get(id: ID, sessionId: UUID, errorStatus: Int): Unit

  def getTests(): Unit = { // Metodin sisällä, jotta entityName bindataan vasta sen jälkeen kun se on overridattu

    s"GET $entityPath/:id" should s"get $entityName from elastic search" in {
      get(existingId, defaultSessionId)
    }

    it should s"have ${KoutaServlet.LastModifiedHeader} header in the response" in {
      get(s"$entityPath/$existingId", headers = Seq(defaultSessionHeader)) {
        status should equal(200)
        header.get(KoutaServlet.LastModifiedHeader) should not be empty
        KoutaServlet.parseHttpDate(header(KoutaServlet.LastModifiedHeader)).toOption should not be empty
      }
    }

    it should s"return 404 if $entityName not found" in {
      get(s"$entityPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
        status should equal(404)
        body should include(s"Didn't find id $nonExistingId from $index")
      }
    }

    it should "return 401 without a valid session" in {
      get(s"$entityPath/$nonExistingId") {
        status should equal(401)
        body should include("Unauthorized")
      }
    }

    it should s"allow a user of the $entityName organization to read the $entityName" in {
      get(existingId, crudSessionIds(ChildOid))
    }

    it should s"deny a user without access to the $entityName organization" in {
      get(existingId, crudSessionIds(LonelyOid), 403)
    }

    it should s"allow a user of an ancestor organization to read the $entityName" in {
      get(existingId, crudSessionIds(ParentOid))
    }

    it should s"allow a user with only access to a descendant organization to read the $entityName" in {
      get(existingId, crudSessionIds(GrandChildOid))
    }

    it should "deny a user with the wrong role" in {
      get(existingId, otherRoleSessionId, 403)
    }

    it should "deny indexer access" in {
      get(existingId, indexerSessionId, 403)
    }
  }
}
