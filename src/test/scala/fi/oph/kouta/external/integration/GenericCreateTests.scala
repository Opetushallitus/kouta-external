package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids.{ChildOid, EvilChildOid, ParentOid}
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoutaIntegrationSpec}
import fi.oph.kouta.security.CasSession

import java.util.UUID

trait GenericCreateTests[E] {
  this: KoutaIntegrationSpec with AccessControlSpec =>

  def entityName: String
  def createdOid: String = ""
  def createdId: String  = ""

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit

  def create(id: String, organisaatioOid: OrganisaatioOid): String
  def create(organisaatioOid: OrganisaatioOid, expectedStatus: Int, expectedBody: String): Unit
  def create(organisaatioOid: OrganisaatioOid, sessionId: UUID): String

  def genericCreateTests(): Unit = {
    s"Create $entityName" should s"create new $entityName and return oid of newly created $entityName" in {
      mockCreate(
        ParentOid,
        if (createdOid.isEmpty) responseStringWithId(createdId) else responseStringWithOid(createdOid)
      )
      create(if (createdOid.isEmpty) createdId else createdOid, ParentOid)
    }

    it should "return the error code and message" in {
      val testError = "{\"error\": \"test error\"}"
      mockCreate(ChildOid, testError, 400)

      create(ChildOid, 400, testError)
    }

    it should "include the caller's authentication in the call" in {
      val (sessionId, session) = crudSessions(EvilChildOid)
      mockCreate(
        EvilChildOid,
        if (createdOid.isEmpty) responseStringWithId(createdId) else responseStringWithOid(createdOid),
        200,
        Some((sessionId, session))
      )
      create(EvilChildOid, sessionId)
    }

  }
}
