package fi.oph.kouta.external.integration

import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, ElasticFixture, HakuFixture, KoulutusFixture}

class ElasticFixtureSpec extends HakuFixture with KoulutusFixture with AccessControlSpec with ElasticFixture {

  val hakuOid = HakuOid("1.2.246.562.29.00000000000000000009")

  "resetIndices and initIndices" should "remove everything from Elasticsearch" in {
    addMockHaku(hakuOid)
    get(hakuOid)
    resetIndices()
    initIndices()
    get(hakuOid, 404)
  }

  //"restoreState"
  // TODO: fails randomly on "no shard available for [get [haku-kouta][_all][1.2.246.562.29.1]" after restore
  ignore should "Restore the state from an earlier saved state" in {
    val oid1 = HakuOid("1.2.246.562.29.1")
    val oid2 = HakuOid("1.2.246.562.29.2")

    addMockHaku(oid1)
    get(oid1)

    saveState("restore_test")

    resetIndices()
    get(oid1, 404)

    restoreState("restore_test")
    get(oid1)

    addMockHaku(oid2)
    get(oid2)

    restoreState("restore_test")
    get(oid2, 404)
  }
}
