package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.{En, Fi, Sv}
import fi.oph.kouta.external.TestData.{KoutaLightKoulutusWithOptionalData, MinKoutaLightKoulutus}
import fi.oph.kouta.external.domain.koutalight.KoutaLightKoulutus
import fi.oph.kouta.external.integration.fixture.KoutaLightFixture
import fi.oph.kouta.external.service.{KoulutusService, KoutaLightService, ValidationError, Validations}
import org.json4s.jackson.JsonMethods.parse

import java.util.UUID

class KoutaLightSpec extends KoutaLightFixture {
  val nonExistingSessionId: UUID             = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")
  val koutaLightKoulutus: KoutaLightKoulutus = KoutaLightKoulutusWithOptionalData

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
    put(List(koutaLightKoulutus), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "8929jwl2"}]"""
    )
  }

  it should "update existing koulutus" in {
    val externalId = "externalId1234"
    val orgOid     = KoutaLightOrgOid1

    val koulutus = koutaLightKoulutus.copy(externalId = externalId)
    put(List(koulutus), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId1234"}]"""
    )

    val koulutusWithUpdatedName =
      koulutus.copy(externalId = externalId, nimi = Map(Fi -> "Päivitetty nimi fi", Sv -> "Päivitetty nimi sv"))
    put(List(koulutusWithUpdatedName), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "UPDATE", "success": true, "externalId": "externalId1234"}]"""
    )

    val storedKoulutus = getFromDb(externalId, orgOid)
    storedKoulutus should contain theSameElementsAs List(koulutusWithUpdatedName)
  }

  it should "create new koulutus with existing id but different owner organization" in {
    val externalId = "externalId4567"
    val orgOid1    = KoutaLightOrgOid1
    val orgOid2    = KoutaLightOrgOid2

    val koulutus1 = koutaLightKoulutus.copy(
      externalId = externalId,
      ammattinimikkeet = List(Map(Fi -> "insinööri", Sv -> "ingenjör"), Map(Fi -> "asiantuntija", Sv -> "expert"))
    )
    put(List(koulutus1), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId4567"}]"""
    )

    val koulutus2 = koutaLightKoulutus.copy(externalId = externalId)
    put(List(koulutus2), koutaLightSessionId2) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId4567"}]"""
    )

    val storedKoulutus1 = getFromDb(externalId, orgOid1)
    storedKoulutus1 should contain theSameElementsAs List(
      koulutus1.copy(ammattinimikkeet =
        List(Map(Fi -> "insinööri"), Map(Sv -> "ingenjör"), Map(Fi -> "asiantuntija"), Map(Sv -> "expert"))
      )
    )
    val storedKoulutus2 = getFromDb(externalId, orgOid2)
    storedKoulutus2 should contain theSameElementsAs List(koulutus2)
  }

  it should "try to create or update several koulutus and return message for each operation" in {
    val externalId1 = "externalId1111"
    val externalId2 = "externalId2222"
    val externalId3 = "externalId3333"

    val koulutus1 = koutaLightKoulutus.copy(
      externalId = externalId1,
      nimi = Map(Fi -> "Koulutus1 nimi fi", Sv -> "Koulutus1 nimi sv")
    )
    put(List(koulutus1), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId1111"}]"""
    )

    val koulutus2 = koutaLightKoulutus.copy(
      externalId = externalId2,
      nimi = Map(Fi -> "Koulutus2 nimi fi", Sv -> "Koulutus2 nimi sv")
    )
    val koulutus3 = koutaLightKoulutus.copy(
      externalId = externalId3,
      nimi = Map(Fi -> "Koulutus3 nimi fi", Sv -> "Koulutus3 nimi sv")
    )
    put(List(koulutus2, koulutus1, koulutus3), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId2222"},
         | {"operation": "UPDATE", "success": true, "externalId": "externalId1111"},
         | {"operation": "CREATE", "success": true, "externalId": "externalId3333"}]""".stripMargin
    )
  }

  "findMissingKielet" should "return empty list as Kielistetty field has all languages from kielivalinta" in {
   Validations.findMissingLanguages(
      List(Fi, Sv, En),
      Map(Fi -> "kuvaus fi", Sv -> "kuvaus sv", En -> "kuvaus en")
    ) shouldEqual List()
  }

  it should "return a list with Sv and En as missing languages when they do not exist in Kielistetty field even though they are defined in kielivalinta" in {
    Validations.findMissingLanguages(List(Fi, Sv, En), Map(Fi -> "kuvaus fi")) shouldEqual List(Sv, En)
  }
  }
}
