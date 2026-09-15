package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.indexed.{AmmatillinenTutkinnonOsaKoulutusMetadataIndexed, KoulutusMetadataIndexed}
import fi.oph.kouta.external.integration.fixture.KoulutusFixture
import fi.oph.kouta.security.Role
import org.json4s.jackson.Serialization.read

class TutkinnonOsaKoulutusSpec extends KoulutusFixture with GenericGetTests[Koulutus, KoulutusOid] with KoutaBackendMock {

  override val roleEntities               = Seq(Role.Koulutus)
  override val entityPath: String         = KoulutusPath
  override val entityName                 = "koulutus"
  override val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000009")
  override val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000000")
  override val throwingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000008")

  val ammTutkinnonOsaOid: KoulutusOid = existingId

  "GET /koulutus/:oid" should "return paikalliset tutkinnon osat for amm-tutkinnon-osa koulutus" in {
    val result = get(ammTutkinnonOsaOid, crudSessionIds(ChildOid))
    result.metadata match {
      case Some(metadata: AmmatillinenTutkinnonOsaKoulutusMetadata) =>
        metadata.paikallisetTutkinnonOsat should contain theSameElementsAs Seq(
          PaikallinenTutkinnonOsa(opetussuunnitelmaId = "123", tutkinnonosaId = "456")
        )
      case other => fail(s"Expected AmmatillinenTutkinnonOsaKoulutusMetadata, got $other")
    }
  }

  "AmmatillinenTutkinnonOsaKoulutusMetadataIndexed" should "default to an empty list for documents indexed before the field existed" in {
    val indexedJsonWithoutField =
      """{
        |  "tyyppi": "amm-tutkinnon-osa",
        |  "kuvaus": {},
        |  "osaamistavoitteet": {},
        |  "lisatiedot": [],
        |  "tutkinnonOsat": [],
        |  "koulutusala": []
        |}""".stripMargin

    val indexed = read[KoulutusMetadataIndexed](indexedJsonWithoutField)

    indexed.asInstanceOf[AmmatillinenTutkinnonOsaKoulutusMetadataIndexed].paikallisetTutkinnonOsat shouldBe Seq()
    indexed.toKoulutusMetadata.asInstanceOf[AmmatillinenTutkinnonOsaKoulutusMetadata].paikallisetTutkinnonOsat shouldBe Seq()
  }
}
