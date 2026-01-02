package fi.oph.kouta.external.integration

import fi.oph.kouta.external.TestData.MinKoutaLightKoulutus
import fi.oph.kouta.external.domain.koutalight.KoutaLightKoulutus
import fi.oph.kouta.external.integration.fixture.KoutaLightFixture
import org.json4s.jackson.JsonMethods.parse

import java.util.UUID

class KoutaLightSpec extends KoutaLightFixture {

  val nonExistingSessionId: UUID             = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")
  val koutaLightKoulutus: KoutaLightKoulutus = MinKoutaLightKoulutus

  s"PUT /koutan-tietomallista-poikkeavat-koulutukset/" should "return 401 without a session" in {
    put(nonExistingSessionId, 401, """{"error":"Unauthorized"}""")
  }

  it should "return 403 when user does not have the right to store koulutukset through the API" in {
    put(
      List(MinKoutaLightKoulutus),
      defaultSessionId,
      403,
      s"""{"errorMessage":"Käyttäjällä ei ole oikeutta koulutusten tallentamiseen rajapinnan kautta"}"""
    )
  }

  it should "return 403 when user has two organizations defined for the KoutaLight user right" in {
    put(
      List(MinKoutaLightKoulutus),
      faultyKoutaLightSessionId,
      403,
      s"""{"errorMessage":"Käyttäjän oikeuksissa määritelty liian monta organisaatiota"}"""
    )
  }

  it should "create a new koulutus" in {
    val result = put(List(koutaLightKoulutus), koutaLightSessionId)

    result shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "8929jwl2"}]"""
    )
  }

  it should "update old koulutus" in {
    val result = put(List(koutaLightKoulutus), koutaLightSessionId)

    result shouldEqual parse(
      s"""[{"operation": "UPDATE", "success": true, "externalId": "8929jwl2"}]"""
    )
  }
}
