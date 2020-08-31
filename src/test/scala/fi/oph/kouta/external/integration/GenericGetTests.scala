package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoutaIntegrationSpec}

trait GenericGetTests[E, ID] {
  this: KoutaIntegrationSpec with AccessControlSpec =>

  def getPath: String
  def entityName: String
  def index: String = s"$entityName-kouta"
  def existingId: ID
  def nonExistingId: ID

  def get(id: ID, sessionId: UUID): E
  def get(id: ID, sessionId: UUID, errorStatus: Int): Unit

  def getTests(): Unit = { // Metodin sisällä, jotta entityName bindataan vasta sen jälkeen kun se on overridattu

    s"GET $getPath/:id" should s"get $entityName from elastic search" in {
      get(existingId, defaultSessionId)
    }

    it should s"return 404 if $entityName not found" in {
      get(s"$getPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
        status should equal(404)
        body should include(s"Didn't find id $nonExistingId from $index")
      }
    }

    it should "return 401 without a valid session" in {
      get(s"$getPath/$nonExistingId") {
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

    it should "deny a user with only access to a descendant organization" in {
      get(existingId, crudSessionIds(GrandChildOid), 403)
    }

    it should "deny a user with the wrong role" in {
      get(existingId, otherRoleSessionId, 403)
    }

    it should "deny indexer access" in {
      get(existingId, indexerSessionId, 403)
    }
  }
}
