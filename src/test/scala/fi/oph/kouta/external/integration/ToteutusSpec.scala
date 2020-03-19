package fi.oph.kouta.external.integration

import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.domain.oid.{KoulutusOid, ToteutusOid}
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoulutusFixture, ToteutusFixture}
import fi.oph.kouta.external.security.Role

class ToteutusSpec extends ToteutusFixture with KoulutusFixture with AccessControlSpec with GenericGetTests[Toteutus, ToteutusOid] {

  override val roleEntities               = Seq(Role.Toteutus)
  override val getPath: String            = ToteutusPath
  override val entityName: String         = "toteutus"
  override val existingId: ToteutusOid    = ToteutusOid("1.2.246.562.17.789")
  override val nonExistingId: ToteutusOid = ToteutusOid("1.2.246.562.17.0")

  val koulutusOid = KoulutusOid("1.2.246.562.13.789")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockKoulutus(koulutusOid, ChildOid)
    addMockToteutus(existingId, ChildOid, koulutusOid)
  }

  getTests()
}