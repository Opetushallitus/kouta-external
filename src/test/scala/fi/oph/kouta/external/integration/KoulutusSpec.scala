package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.integration.fixture.KoulutusFixture
import fi.oph.kouta.security.Role

import java.util.UUID

class KoulutusSpec extends KoulutusFixture with GenericGetTests[Koulutus, KoulutusOid]{

  override val roleEntities               = Seq(Role.Koulutus)
  override val getPath: String = KoulutusPath
  override val entityName   = "koulutus"
  override val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000001")
  override val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000000")

  val nonExistingSessionId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  val ophKoulutusOid: KoulutusOid =           KoulutusOid("1.2.246.562.13.00000000000000000002")
  val julkinenOid: KoulutusOid =              KoulutusOid("1.2.246.562.13.00000000000000000003")
  val tarjoajaOid: KoulutusOid =              KoulutusOid("1.2.246.562.13.00000000000000000004")
  val ammMuuOid: KoulutusOid =                KoulutusOid("1.2.246.562.13.00000000000000000005")
  val aikuistenPerusopetusOid: KoulutusOid =  KoulutusOid("1.2.246.562.13.00000000000000000006")

  val sorakuvausId: UUID         = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  getTests()

  it should "return 401 without a session" in {
    get(nonExistingId, nonExistingSessionId, 401)
  }

  it should "allow the user of proper koulutustyyppi to read koulutus created by oph" in {
    get(ophKoulutusOid, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read koulutus created by oph" in {
    get(ophKoulutusOid, readSessionIds(YoOid), 403)
  }

  it should "allow the user of proper koulutustyyppi to read julkinen koulutus" in {
    get(julkinenOid, readSessionIds(ChildOid))
  }

  it should "deny the user of wrong koulutustyyppi to read julkinen koulutus" in {
    get(julkinenOid, readSessionIds(YoOid), 403)
  }

  it should "allow the user of a tarjoaja organization to read the koulutus" in {
    get(tarjoajaOid, readSessionIds(ChildOid))
  }

  it should "allow the user of an ancestor of a tarjoaja organization" in {
    get(tarjoajaOid, crudSessionIds(ParentOid))
  }

  it should "allow the user of a descendant of a tarjoaja organization" in {
    get(tarjoajaOid, crudSessionIds(GrandChildOid))
  }

  it should "allow the user to read muu ammatillinen koulutus" in {
    get(ammMuuOid, crudSessionIds(ChildOid))
  }

  it should "allow the user to read aikuisten perusopetus -koulutus" in {
    get(aikuistenPerusopetusOid, crudSessionIds(ChildOid))
  }
}
