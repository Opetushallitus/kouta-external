package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.integration.fixture.{KoutaIntegrationSpec, MassToteutusFixture}
import fi.oph.kouta.security.CasSession
import org.json4s.jackson.JsonMethods.parse

import java.time.Instant
import java.util.UUID

class MassToteutusSpec extends KoutaBackendMock with MassToteutusFixture with KoutaIntegrationSpec {

  val nonExistingSessionId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  def mockCreate(
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      "toteutus",
      KoutaBackendConverters.convertToteutus(toteutus),
      "kouta-backend.toteutus",
      responseString,
      session,
      responseStatus
    )

  def mockUpdate(
      oid: ToteutusOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addUpdateMock(
      "toteutus",
      KoutaBackendConverters.convertToteutus(toteutus(oid)),
      "kouta-backend.toteutus",
      Some(Instant.now()),
      session,
      responseString,
      responseStatus
    )

  def responseStringWithUpdated(updated: Boolean): String =
    s"""{"updated": $updated}"""

  def nonsenseResponseString: String =
    """{"nonsense": true}"""

  s"PUT /toteutukset/" should "return 401 without a session" in {
    put(nonExistingSessionId, 401, """{"error":"Unauthorized"}""")
  }

  it should "create a new toteutus when called without an oid" in {
    mockCreate(responseStringWithOid(toteutusOid.s))

    val result = put(List(toteutus))

    result shouldEqual parse(s"""[{"operation": "CREATE", "success": true, "oid": "${toteutusOid.s}"}]""")
  }

  it should "update an existing toteutus when called with an oid" in {
    mockUpdate(toteutusOid, responseStringWithUpdated(true))

    val result = put(List(toteutus(toteutusOid)))

    result shouldEqual parse(s"""[{"operation": "UPDATE", "success": true, "updated": true}]""")
  }

  it should "create and update when called with both" in {
    mockCreate(responseStringWithOid(toteutusOid.s))
    mockUpdate(toteutusOid, responseStringWithUpdated(true))

    val result = put(List(toteutus, toteutus(toteutusOid)))

    result shouldEqual parse(
      s"""[
         |  {"operation": "CREATE", "success": true, "oid": "${toteutusOid.s}"},
         |  {"operation": "UPDATE", "success": true, "updated": true}
         |]""".stripMargin)
  }

  it should "respond with error when one operation fails" in {
    mockCreate("""{"error": "Test error"}""", 403)
    mockUpdate(toteutusOid, responseStringWithUpdated(true))

    val result = put(List(toteutus(toteutusOid), toteutus))

    result shouldEqual parse(
      s"""[
         |  {"operation": "UPDATE", "success": true, "updated": true},
         |  {"operation": "CREATE", "success": false, "status": 403, "message": "{\\"error\\": \\"Test error\\"}"}
         |]""".stripMargin)
  }

  it should "continue when one operation throws an exception" in {
    mockCreate(nonsenseResponseString)
    mockUpdate(toteutusOid, responseStringWithUpdated(true))

    val result = put(List(toteutus, toteutus(toteutusOid)))

    result shouldEqual parse(
      s"""[
         |  {"operation": "CREATE", "success": false, "exception": "scala.MatchError"},
         |  {"operation": "UPDATE", "success": true, "updated": true}
         |]""".stripMargin)
  }

  it should "returns earlier results after an exception" in {
    mockCreate(responseStringWithOid(toteutusOid.s))
    mockUpdate(toteutusOid, nonsenseResponseString)

    val result = put(List(toteutus, toteutus(toteutusOid)))

    result shouldEqual parse(
      s"""[
         |  {"operation": "CREATE", "success": true, "oid": "${toteutusOid.s}"},
         |  {"operation": "UPDATE", "success": false, "exception": "org.json4s.package.MappingException"}
         |]""".stripMargin)
  }
}
