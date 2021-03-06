package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{KoulutusOid, ToteutusOid}
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoulutusFixture, ToteutusFixture}
import fi.oph.kouta.security.Role

import java.util.UUID

class ToteutusSpec
    extends ToteutusFixture
    with KoulutusFixture
    with AccessControlSpec
    with GenericGetTests[Toteutus, ToteutusOid] {

  override val roleEntities               = Seq(Role.Toteutus)
  override val getPath: String            = ToteutusPath
  override val entityName: String         = "toteutus"
  override val existingId: ToteutusOid    = ToteutusOid("1.2.246.562.17.789")
  override val nonExistingId: ToteutusOid = ToteutusOid("1.2.246.562.17.0")

  val koulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.789")
  val sorakuvausId: UUID       = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  val toteutusWithTarjoajaOid: ToteutusOid = ToteutusOid("1.2.246.562.17.00000000000000000004")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockSorakuvaus(sorakuvausId, ChildOid)
    addMockKoulutus(koulutusOid, sorakuvausId, ChildOid)
    addMockToteutus(existingId, ChildOid, koulutusOid)
    addMockToteutus(toteutusWithTarjoajaOid, LonelyOid, koulutusOid, _ + (KoutaFixtureTool.TarjoajatKey -> ChildOid.s))
  }

  getTests()

  it should "allow the user of a tarjoaja organization" in {
    get(toteutusWithTarjoajaOid, readSessionIds(ChildOid))
  }

  it should "allow the user of an ancestor of a tarjoaja organization" in {
    get(toteutusWithTarjoajaOid, crudSessionIds(ParentOid))
  }

  it should "allow the user of a descendant of a tarjoaja organization" in {
    get(toteutusWithTarjoajaOid, crudSessionIds(GrandChildOid))
  }
}
