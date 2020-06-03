package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoulutusFixture}
import fi.oph.kouta.security.Role

class KoulutusSpec extends KoulutusFixture with AccessControlSpec with GenericGetTests[Koulutus, KoulutusOid] {

  override val roleEntities               = Seq(Role.Koulutus)
  override val getPath: String            = KoulutusPath
  override val entityName: String         = "koulutus"
  override val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000009")
  override val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.0")

  override def beforeAll(): Unit = {
    super.beforeAll()

    println("ASDF KoulutusSpec beforeAll()")
    addMockKoulutus(existingId, ChildOid)
  }

  getTests()
}
