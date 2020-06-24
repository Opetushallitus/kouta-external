package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoulutusFixture}
import fi.oph.kouta.security.Role

class KoulutusSpec extends KoulutusFixture with AccessControlSpec with GenericGetTests[Koulutus, KoulutusOid] {

  override val roleEntities               = Seq(Role.Koulutus)
  override val getPath: String            = KoulutusPath
  override val entityName: String         = "koulutus"
  override val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000009")
  override val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.0")

  val ophKoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000001")
  val julkinenOid    = KoulutusOid("1.2.246.562.13.00000000000000000003")
  val tarjoajaOid    = KoulutusOid("1.2.246.562.13.00000000000000000004")

  override def beforeAll(): Unit = {
    super.beforeAll()

    addMockKoulutus(existingId, ChildOid)
    addMockKoulutus(ophKoulutusOid, OphOid)
    addMockKoulutus(julkinenOid, LonelyOid, _ + (KoutaFixtureTool.JulkinenKey  -> "true"))
    addMockKoulutus(tarjoajaOid, LonelyOid, _ + (KoutaFixtureTool.TarjoajatKey -> ChildOid.s))
  }

  getTests()

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

  it should "allow the user of a tarjoaja organization" in {
    get(tarjoajaOid, readSessionIds(ChildOid))
  }

  it should "allow the user of an ancestor of a tarjoaja organization" in {
    get(tarjoajaOid, crudSessionIds(ParentOid))
  }

  it should "allow the user of a descendant of a tarjoaja organization" in {
    get(tarjoajaOid, crudSessionIds(GrandChildOid))
  }
}
