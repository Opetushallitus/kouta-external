package fi.oph.kouta.external.integration

import fi.oph.kouta.domain.{En, Fi, Sv}
import fi.oph.kouta.external.TestData.{KoutaLightKoulutusWithOptionalData, MinKoutaLightKoulutus}
import fi.oph.kouta.external.integration.fixture.KoutaLightFixture
import fi.oph.kouta.external.service.{KoutaLightService, ValidationError, Validations}
import fi.oph.kouta.koutalight.domain.{ExternalKoutaLightKoulutus, KoutaLightKoulutus}
import org.json4s.jackson.JsonMethods.parse

import java.net.URI
import java.util.UUID

class KoutaLightSpec extends KoutaLightFixture {
  val nonExistingSessionId: UUID             = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")
  val koutaLightKoulutus: ExternalKoutaLightKoulutus = KoutaLightKoulutusWithOptionalData

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

  it should "return validation error messages for one koulutus that fails nimi and kuvaus validation" in {
    val koulutus1 = koutaLightKoulutus.copy(
      externalId = "externalId21",
      nimi = Map(Fi -> "Koulutus1 nimi fi", Sv -> "Koulutus1 nimi sv")
    )

    val koulutus2 = koutaLightKoulutus.copy(
      externalId = "externalId22",
      nimi = Map(Fi -> "Koulutus2 nimi fi"),
      kuvaus = Map(Fi -> "Koulutus2 kuvaus fi")
    )

    put(List(koulutus1, koulutus2), koutaLightSessionId1) shouldEqual parse(
      s"""[{"operation": "CREATE", "success": true, "externalId": "externalId21"},
         | {"operation": "CREATE OR UPDATE",
         | "success": false,
         | "externalId": "externalId22",
         | "exception": "Kielistetystä kentästä 'nimi' puuttuu arvo kielillä [sv]"},
         | {"operation": "CREATE OR UPDATE",
         | "success": false,
         | "externalId": "externalId22",
         | "exception": "Kielistetystä kentästä 'kuvaus' puuttuu arvo kielillä [sv]"}]""".stripMargin
    )
  }

  "findMissingLanguages" should "return empty list as Kielistetty field has all languages from kielivalinta" in {
    Validations.findMissingLanguages(
      List(Fi, Sv, En),
      Left(Map(Fi -> "kuvaus fi", Sv -> "kuvaus sv", En -> "kuvaus en"))
    ) shouldEqual List()
  }

  it should "return a list with Sv and En as missing languages when they do not exist in Kielistetty field even though they are defined in kielivalinta" in {
    Validations.findMissingLanguages(List(Fi, Sv, En), Left(Map(Fi -> "kuvaus fi"))) shouldEqual List(Sv, En)
  }

  "validateKielistetty" should "return empty list of validation errors when kielistetty kuvaus has value for all languages defined in kielivalinta" in {
    Validations.validateKielistetty(
      List(Fi, Sv, En),
      Left(Map(Fi -> "kuvaus fi", Sv -> "kuvaus sv", En -> "kuvaus en")),
      "externalId6357",
      "kuvaus"
    ) shouldEqual List()
  }

  it should "return list of validation errors when kielistetty kuvaus has value for all languages defined in kielivalinta" in {
    val externalId = "externalId6357"
    Validations.validateKielistetty(
      List(Fi, Sv, En),
      Left(Map(Fi -> "kuvaus fi")),
      externalId,
      "kuvaus"
    ) shouldEqual List(
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'kuvaus' puuttuu arvo kielillä [sv, en]"""
      )
    )
  }

  "validate" should "return validation error when kuvaus has only Finnish translation even though kielivalinta has Finnish and Swedish defined" in {
    val externalId = "externalId321"
    val koulutus =
      MinKoutaLightKoulutus.copy(externalId = externalId, kielivalinta = List(Fi, Sv), kuvaus = Map(Fi -> "kuvaus fi"))
    KoutaLightService.validate(koulutus) should contain theSameElementsAs List(
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'kuvaus' puuttuu arvo kielillä [sv]"""
      )
    )
  }

  it should "return validation errors for all kielistetty fields as they have only Finnish translation even though kielivalinta has Finnish and Swedish defined" in {
    val externalId = "externalId321"
    val koulutus =
      MinKoutaLightKoulutus.copy(
        externalId = externalId,
        kielivalinta = List(Fi, Sv),
        nimi = Map(Fi -> "nimi fi"),
        tarjoajat = List(Map(Fi -> "tarjoajan nimi fi"), Map(En -> "toisen tarjoajan nimi en")),
        kuvaus = Map(Fi -> "kuvaus fi"),
        ammattinimikkeet =
          List(Map(Fi -> "ammattinimike 1 fi", Sv -> "ammattinimike 1 sv"), Map(Sv -> "ammattinimike 2 sv")),
        asiasanat = List(Map(Fi -> "asiasana 1 fi"), Map(Fi -> "asiasana 2 fi", Sv -> "asiasana 2 sv")),
        hakulomakeLinkki = Map(Fi -> new URI("https://opintopolku.fi").toURL),
        maksullisuuskuvaus = Map(Fi -> "maksullisuuskuvaus 1 fi")
      )
    KoutaLightService.validate(koulutus) should contain theSameElementsAs List(
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'nimi' puuttuu arvo kielillä [sv]"""
      ),
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'tarjoajat[0]' puuttuu arvo kielillä [sv]"""
      ),
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'tarjoajat[1]' puuttuu arvo kielillä [fi, sv]"""
      ),
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'kuvaus' puuttuu arvo kielillä [sv]"""
      ),
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'ammattinimikkeet[1]' puuttuu arvo kielillä [fi]"""
      ),
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'asiasanat[0]' puuttuu arvo kielillä [sv]"""
      ),
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'hakulomakeLinkki' puuttuu arvo kielillä [sv]"""
      ),
      ValidationError(
        koulutusExternalId = externalId,
        message = """Kielistetystä kentästä 'maksullisuuskuvaus' puuttuu arvo kielillä [sv]"""
      )
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
}
