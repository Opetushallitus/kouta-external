package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid}
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, KoulutusFixture}
import fi.oph.kouta.security.{CasSession, Role}

import java.time.Instant
import java.util.UUID

class KoulutusSpec
    extends KoulutusFixture
    with AccessControlSpec
    with GenericGetTests[Koulutus, KoulutusOid]
    with GenericCreateTests[Koulutus]
    with GenericUpdateTests[Koulutus]
    with KoutaBackendMock {

  override val roleEntities               = Seq(Role.Koulutus)
  override val entityPath: String         = KoulutusPath
  override val entityName                 = "koulutus"
  override val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000001")
  override val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000000")
  override val createdOid                 = "1.2.246.562.13.123456789"
  override val updatedOidBase             = "1.2.246.562.13.1"

  val nonExistingSessionId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  val ophKoulutusOid: KoulutusOid          = KoulutusOid("1.2.246.562.13.00000000000000000002")
  val julkinenOid: KoulutusOid             = KoulutusOid("1.2.246.562.13.00000000000000000003")
  val tarjoajaOid: KoulutusOid             = KoulutusOid("1.2.246.562.13.00000000000000000004")
  val ammMuuOid: KoulutusOid               = KoulutusOid("1.2.246.562.13.00000000000000000005")
  val aikuistenPerusopetusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000006")

  val sorakuvausId: UUID = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      entityName,
      KoutaBackendConverters.convertKoulutus(koulutus(organisaatioOid)),
      "kouta-backend.koulutus",
      responseString,
      session,
      responseStatus
    )

  def mockUpdate(
      oidOrId: String,
      ifUnmodifiedSince: Option[Instant],
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addUpdateMock(
      entityName,
      KoutaBackendConverters.convertKoulutus(koulutus(oidOrId)),
      "kouta-backend.koulutus",
      ifUnmodifiedSince,
      session,
      responseString,
      responseStatus
    )

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

  genericCreateTests()

  genericUpdateTests()
}
