package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.oid.HakukohdeOid
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.integration.fixture.{KoutaIntegrationSpec, MassHakukohdeFixture}
import fi.oph.kouta.security.CasSession
import org.json4s.jackson.JsonMethods.parse

import java.time.Instant
import java.util.UUID

class MassHakukohdeSpec extends KoutaBackendMock with MassHakukohdeFixture with KoutaIntegrationSpec {

  val nonExistingSessionId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  def mockCreate(
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      "hakukohde",
      KoutaBackendConverters.convertHakukohde(hakukohde),
      "kouta-backend.hakukohde",
      responseString,
      session,
      responseStatus
    )

  def mockUpdate(
      oid: HakukohdeOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addUpdateMock(
      "hakukohde",
      KoutaBackendConverters.convertHakukohde(hakukohde(oid)),
      "kouta-backend.hakukohde",
      Some(Instant.now()),
      session,
      responseString,
      responseStatus
    )

  def responseStringWithUpdated(updated: Boolean): String =
    s"""{"updated": $updated}"""

  def nonsenseResponseString: String =
    """{"nonsense": true}"""

  s"PUT /hakukohteet/" should "return 401 without a session" in {
    put(nonExistingSessionId, 401, """{"error":"Unauthorized"}""")
  }

  it should "return 400 when called with duplicate oids" in {
    put(List(hakukohde(hakukohdeOid), hakukohde(hakukohdeOid)), 400, s"""{"error":"Pyynnössä oli monta kohdetta, joilla oli sama OID: $hakukohdeOid"}""")
  }

  it should "create a new hakukohde when called without an oid" in {
    mockCreate(responseStringWithOid(hakukohdeOid.s))

    val result = put(List(hakukohde))

    result shouldEqual parse(s"""[{"operation": "CREATE", "success": true, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde"}]""")
  }

  it should "update an existing hakukohde when called with an oid" in {
    mockUpdate(hakukohdeOid, responseStringWithUpdated(true))

    val result = put(List(hakukohde(hakukohdeOid)))

    result shouldEqual parse(s"""[{"operation": "UPDATE", "success": true, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde", "updated": true}]""")
  }

  it should "create and update when called with both" in {
    mockCreate(responseStringWithOid(hakukohdeOid.s))
    mockUpdate(hakukohdeOid, responseStringWithUpdated(true))

    val result = put(List(hakukohde, hakukohde(hakukohdeOid)))

    result shouldEqual parse(s"""[
                                |  {"operation": "CREATE", "success": true, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde"},
                                |  {"operation": "UPDATE", "success": true, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde", "updated": true}
                                |]""".stripMargin)
  }

  it should "respond with error when one operation fails" in {
    mockCreate("""{"error": "Test error"}""", 403)
    mockUpdate(hakukohdeOid, responseStringWithUpdated(true))

    val result = put(List(hakukohde(hakukohdeOid), hakukohde))

    result shouldEqual parse(s"""[
                                |  {"operation": "UPDATE", "success": true, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde", "updated": true},
                                |  {"operation": "CREATE", "success": false, "externalId": "extHakukohde", "status": 403, "message": "{\\"error\\": \\"Test error\\"}"}
                                |]""".stripMargin)
  }

  it should "continue when one operation throws an exception" in {
    mockCreate(nonsenseResponseString)
    mockUpdate(hakukohdeOid, responseStringWithUpdated(true))

    val result = put(List(hakukohde, hakukohde(hakukohdeOid)))

    result shouldEqual parse(s"""[
                                |  {"operation": "CREATE", "success": false, "externalId": "extHakukohde", "exception": "scala.MatchError"},
                                |  {"operation": "UPDATE", "success": true, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde", "updated": true}
                                |]""".stripMargin)
  }

  it should "returns earlier results after an exception" in {
    mockCreate(responseStringWithOid(hakukohdeOid.s))
    mockUpdate(hakukohdeOid, nonsenseResponseString)

    val result = put(List(hakukohde, hakukohde(hakukohdeOid)))

    result shouldEqual parse(s"""[
                                |  {"operation": "CREATE", "success": true, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde"},
                                |  {"operation": "UPDATE", "success": false, "oid": "${hakukohdeOid.s}", "externalId": "extHakukohde", "exception": "org.json4s.package.MappingException"}
                                |]""".stripMargin)
  }
}
