package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids.EvilChildOid
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoutaIntegrationSpec}
import fi.oph.kouta.security.CasSession

import java.time.Instant
import java.util.UUID

trait GenericUpdateTests[E] {
  this: KoutaIntegrationSpec with AccessControlSpec =>

  def entityName: String
  def updatedOidBase: String = ""
  def updatedIdBase: String  = ""

  def mockUpdate(
      oidOrId: String,
      ifUnmodifiedSince: Option[Instant],
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit

  def update(oidOrId: String, ifUnmodifiedSince: Instant): Unit
  def update(oidOrId: String, ifUnmodifiedSince: Option[Instant], responseStatus: Int, responseString: String): Unit
  def update(oidOrId: String, ifUnmodifiedSince: Instant, sessionId: UUID): Unit

  def theId(uniqueNumber: Int) =
    if (updatedOidBase.isEmpty) updatedIdBase + uniqueNumber else updatedOidBase + uniqueNumber

  def genericUpdateTests(): Unit = {
    s"Update $entityName" should s"execute update successfully" in {
      val now = Instant.now()
      mockUpdate(theId(1), Some(now), s"""{"updated": true}""")
      update(theId(1), now)
    }

    it should "require x-If-Unmodified-Since header" in {
      update(theId(2), None, 400, s"""{"error":"Otsake x-If-Unmodified-Since on pakollinen."}""")
    }

    it should s"return the error code and message when updating $entityName" in {
      val testError = s"""{"error": "test error"}"""
      val now       = Instant.now()

      mockUpdate(theId(3), Some(now), testError, 400)
      update(theId(3), Some(now), 400, testError)
    }

    it should s"include the caller's authentication in the call when updating $entityName" in {
      val now                  = Instant.now()
      val (sessionId, session) = crudSessions(EvilChildOid)

      mockUpdate(theId(4), Some(now), s"""{"updated": true}""", session = Some((sessionId, session)))
      update(theId(4), now, sessionId)
    }
  }
}
