package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.integration.fixture.{KoutaIntegrationSpec, MassKoulutusFixture}
import fi.oph.kouta.security.CasSession
import org.json4s.jackson.JsonMethods.parse

import java.time.Instant
import java.util.UUID

class MassKoulutusSpec extends KoutaBackendMock with MassKoulutusFixture with KoutaIntegrationSpec {

  val nonExistingSessionId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  def mockCreate(
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None,
      koulutus: Koulutus = koulutus
  ): Unit =
    addCreateMock(
      "koulutus",
      KoutaBackendConverters.convertKoulutus(koulutus),
      "kouta-backend.koulutus",
      responseString,
      session,
      responseStatus
    )

  def mockUpdate(
      oid: KoulutusOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addUpdateMock(
      "koulutus",
      KoutaBackendConverters.convertKoulutus(koulutus(oid)),
      "kouta-backend.koulutus",
      Some(Instant.now()),
      session,
      responseString,
      responseStatus
    )

  def responseStringWithUpdated(updated: Boolean): String =
    s"""{"updated": $updated}"""

  def nonsenseResponseString: String =
    """{"nonsense": true}"""

  s"PUT /koulutukset/" should "return 401 without a session" in {
    put(nonExistingSessionId, 401, """{"error":"Unauthorized"}""")
  }

  it should "create a new koulutus when called without an oid" in {
    mockCreate(responseStringWithOid(koulutusOid.s))

    val result = put(List(koulutus))

    result shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1"}]"""
    )
  }

  it should "create a new koulutus when called without an oid and without external ID" in {
    val koulutusWithoutExternalId = koulutus.copy(externalId = None)
    mockCreate(responseStringWithOid(koulutusOid.s), koulutus = koulutusWithoutExternalId)

    val result = put(List(koulutusWithoutExternalId))

    result shouldEqual parse(s"""[{"operation": "CREATE", "success": true, "oid": "${koulutusOid.s}"}]""")
  }

  it should "update an existing koulutus when called with an oid" in {
    mockUpdate(koulutusOid, responseStringWithUpdated(true))

    val result = put(List(koulutus(koulutusOid)))

    result shouldEqual parse(s"""[{"operation": "UPDATE", "success": true, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1", "updated": true}]""")
  }

  it should "create and update when called with both" in {
    mockCreate(responseStringWithOid(koulutusOid.s))
    mockUpdate(koulutusOid, responseStringWithUpdated(true))

    val result = put(List(koulutus, koulutus(koulutusOid)))

    result shouldEqual parse(
      s"""[
         |  {"operation": "CREATE", "success": true, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1"},
         |  {"operation": "UPDATE", "success": true, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1", "updated": true}
         |]""".stripMargin)
  }

  it should "respond with error when one operation fails" in {
    mockCreate("""{"error": "Test error"}""", 403)
    mockUpdate(koulutusOid, responseStringWithUpdated(true))

    val result = put(List(koulutus(koulutusOid), koulutus))

    result shouldEqual parse(
      s"""[
         |  {"operation": "UPDATE", "success": true, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1", "updated": true},
         |  {"operation": "CREATE", "success": false, "externalId": "extKoulutus1", "status": 403, "message": "{\\"error\\": \\"Test error\\"}"}
         |]""".stripMargin)
  }

  it should "continue when one operation throws an exception" in {
    mockCreate(nonsenseResponseString)
    mockUpdate(koulutusOid, responseStringWithUpdated(true))

    val result = put(List(koulutus, koulutus(koulutusOid)))

    result shouldEqual parse(
      s"""[
         |  {"operation": "CREATE", "success": false, "externalId": "extKoulutus1", "exception": "scala.MatchError"},
         |  {"operation": "UPDATE", "success": true, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1", "updated": true}
         |]""".stripMargin)
  }

  it should "returns earlier results after an exception" in {
    mockCreate(responseStringWithOid(koulutusOid.s))
    mockUpdate(koulutusOid, nonsenseResponseString)

    val result = put(List(koulutus, koulutus(koulutusOid)))

    result shouldEqual parse(
      s"""[
         |  {"operation": "CREATE", "success": true, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1"},
         |  {"operation": "UPDATE", "success": false, "oid": "${koulutusOid.s}", "externalId": "extKoulutus1", "exception": "org.json4s.package.MappingException"}
         |]""".stripMargin)
  }
}
