package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.{Fi, Sv}
import fi.oph.kouta.external.TestData.{KoutaLightKoulutusWithOptionalData, MinKoutaLightKoulutus}
import fi.oph.kouta.external.domain.{ExternalKoutaLightKoulutus, KoutaLightKoulutus}
import fi.oph.kouta.external.integration.fixture.KoutaLightFixture
import fi.oph.kouta.external.service.{KoutaLightService, ValidationError}
import org.json4s.jackson.JsonMethods.parse

import java.util.UUID

class KoutaLightSpec extends KoutaLightFixture {
  val nonExistingSessionId: UUID                     = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")
  val koutaLightKoulutus: ExternalKoutaLightKoulutus = KoutaLightKoulutusWithOptionalData

  s"PUT /koutan-tietomallista-poikkeavat-koulutukset/" should "return 401 without a session" in {
    put(nonExistingSessionId, 401, """{"error":"Unauthorized"}""")
  }

  it should "return 403 when user does not have the right to store koulutukset through the API" in {
    put(
      List(MinKoutaLightKoulutus),
      defaultSessionId,
      403,
      s"""{"error":"Käyttäjällä ${testUser.oid} ei ole oikeutta koulutusten tallentamiseen rajapinnan kautta."}"""
    )
  }

  it should "return 403 when user has two organizations defined for the KoutaLight user right" in {
    put(
      List(MinKoutaLightKoulutus),
      faultyKoutaLightSessionWithMultipleOrgs._1,
      403,
      s"""{"error":"Käyttäjän ${testUser.oid} oikeuksissa määritelty liian monta organisaatiota."}"""
    )
  }

  it should "return 403 when no org attached to the user role" in {
    put(
      List(MinKoutaLightKoulutus),
      faultyKoutaLightSessionWithoutOrg._1,
      403,
      s"""{"error":"Käyttäjän ${testUser.oid} oikeuksissa puutteita."}"""
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

    val storedKoulutukset = getFromDb(externalId, orgOid)
    val storedKoulutus    = storedKoulutukset.head
    val storedKoulutusWithUpdatedName = KoutaLightKoulutus(orgOid, koulutusWithUpdatedName).copy(
      id = storedKoulutus.id,
      createdAt = storedKoulutus.createdAt,
      updatedAt = storedKoulutus.updatedAt
    )
    storedKoulutukset should contain theSameElementsAs List(storedKoulutusWithUpdatedName)
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

    val storedKoulutukset1 = getFromDb(externalId, orgOid1)
    val storedKoulutus1    = storedKoulutukset1.head
    val koulutus1WithMetadata = KoutaLightKoulutus(orgOid1, koulutus1).copy(
      id = storedKoulutus1.id,
      createdAt = storedKoulutus1.createdAt,
      updatedAt = storedKoulutus1.updatedAt
    )
    storedKoulutukset1 should contain theSameElementsAs List(koulutus1WithMetadata)

    val storedKoulutukset2 = getFromDb(externalId, orgOid2)
    val storedKoulutus2    = storedKoulutukset2.head
    val koulutus2WithMetadata = KoutaLightKoulutus(orgOid2, koulutus2).copy(
      id = storedKoulutus2.id,
      createdAt = storedKoulutus2.createdAt,
      updatedAt = storedKoulutus2.updatedAt
    )
    storedKoulutukset2 should contain theSameElementsAs List(koulutus2WithMetadata)
  }

  it should "try to create or update several koulutus and return message for each operation" in {
    val koulutus1 = koutaLightKoulutus.copy(
      externalId = "externalId1111",
      nimi = Map(Fi -> "Koulutus1 nimi fi", Sv -> "Koulutus1 nimi sv")
    )
    put(List(koulutus1), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId1111"}]"""
    )

    val koulutus2 = koutaLightKoulutus.copy(
      externalId = "externalId2222",
      nimi = Map(Fi -> "Koulutus2 nimi fi", Sv -> "Koulutus2 nimi sv")
    )
    val koulutus3 = koutaLightKoulutus.copy(
      externalId = "externalId3333",
      nimi = Map(Fi -> "Koulutus3 nimi fi", Sv -> "Koulutus3 nimi sv")
    )
    put(List(koulutus2, koulutus1, koulutus3), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId2222"},
         | {"operation": "UPDATE", "success": true, "externalId": "externalId1111"},
         | {"operation": "CREATE", "success": true, "externalId": "externalId3333"}]""".stripMargin
    )
  }

  it should """return validation error for opetuskielet that has "suomi" and "r" as values can only be 2-3 chars long language codes""" in {
    val externalId = "externalId321"
    val koulutus   = MinKoutaLightKoulutus.copy(externalId = externalId, opetuskielet = List("et", "suomi", "en", "r"))
    KoutaLightService.validate(koulutus) should contain theSameElementsAs List(
      ValidationError(
        koulutusExternalId = externalId,
        message = """Virheellinen arvo [suomi, r] kentässä 'opetuskielet'"""
      )
    )
  }

  it should "return validation error messages for one valid koulutus and one koulutus that fails opetuskielet validation" in {
    val koulutus1 = koutaLightKoulutus.copy(
      externalId = "externalId21",
      nimi = Map(Fi -> "Koulutus1 nimi fi", Sv -> "Koulutus1 nimi sv")
    )

    val koulutus2 = koutaLightKoulutus.copy(
      externalId = "externalId22",
      nimi = Map(Fi -> "Koulutus2 nimi fi"),
      kuvaus = Map(Fi -> "Koulutus2 kuvaus fi"),
      opetuskielet = List("suomi", "r", "fi", "sv")
    )

    put(List(koulutus1, koulutus2), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId21"},
         | {"operation": "CREATE OR UPDATE",
         | "success": false,
         | "externalId": "externalId22",
         | "exception": "Virheellinen arvo [suomi, r] kentässä 'opetuskielet'"}]""".stripMargin
    )
  }
}
