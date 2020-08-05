package fi.oph.kouta.external.integration

import java.util.UUID

import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.oid.{HakuOid, HakukohdeOid, KoulutusOid, ToteutusOid}
import fi.oph.kouta.external.integration.fixture._
import fi.oph.kouta.external.security.Role

class HakukohdeSpec
    extends HakukohdeFixture
    with HakuFixture
    with KoulutusFixture
    with ToteutusFixture
    with ValintaperusteFixture
    with AccessControlSpec
    with GenericGetTests[Hakukohde, HakukohdeOid] {

  override val roleEntities                = Seq(Role.Hakukohde)
  override val getPath: String             = HakukohdePath
  override val entityName: String          = "hakukohde"
  override val existingId: HakukohdeOid    = HakukohdeOid("1.2.246.562.20.00000000000000000009")
  override val nonExistingId: HakukohdeOid = HakukohdeOid("1.2.246.562.20.0")

  val hakuOid          = HakuOid("1.2.246.562.29.00000000000000000010")
  val toteutusId       = ToteutusOid("1.2.246.562.17.00000000000000000010")
  val koulutusOid      = KoulutusOid("1.2.246.562.13.00000000000000000010")
  val valintaperusteId = UUID.fromString("fa7fcb96-3f80-4162-8d19-5b74731cf90c")
  val sorakuvausId     = UUID.fromString("e17773b2-f5a0-418d-a49f-34578c4b3625")

  override def beforeAll(): Unit = {
    super.beforeAll()

    addMockHaku(hakuOid, ChildOid)
    addMockKoulutus(koulutusOid, ChildOid)
    addMockToteutus(toteutusId, ChildOid, koulutusOid)

    addMockSorakuvaus(sorakuvausId, ChildOid)
    addMockValintaperuste(valintaperusteId, ChildOid, sorakuvausId)

    addMockHakukohde(existingId, ChildOid, hakuOid, toteutusId, valintaperusteId)
  }

  getTests()
}
