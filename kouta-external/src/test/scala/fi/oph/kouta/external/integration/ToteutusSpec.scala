package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{KoulutusOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, ToteutusFixture}
import fi.oph.kouta.security.{CasSession, Role}

import java.time.Instant
import java.util.UUID

class ToteutusSpec
    extends ToteutusFixture
    with AccessControlSpec
    with GenericGetTests[Toteutus, ToteutusOid]
    with GenericCreateTests[Toteutus]
    with GenericUpdateTests[Toteutus]
    with KoutaBackendMock {

  override val roleEntities               = Seq(Role.Toteutus)
  override val entityPath: String         = ToteutusPath
  override val entityName: String         = "toteutus"
  override val existingId: ToteutusOid    = ToteutusOid("1.2.246.562.17.00000000000000000001")
  override val nonExistingId: ToteutusOid = ToteutusOid("1.2.246.562.17.00000000000000000000")
  override val throwingId: ToteutusOid    = ToteutusOid("1.2.246.562.17.00000000000000000003")

  val koulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.789")
  val sorakuvausId: UUID       = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  val toteutusWithTarjoajaOid: ToteutusOid = ToteutusOid("1.2.246.562.17.00000000000000000002")

  override val createdOid       = "1.2.246.562.17.123456789"
  override val updatedOidBase   = "1.2.246.562.17.1"

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      entityName,
      KoutaBackendConverters.convertToteutus(toteutus(organisaatioOid)),
      "kouta-backend.toteutus",
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
      KoutaBackendConverters.convertToteutus(toteutus(oidOrId)),
      "kouta-backend.toteutus",
      ifUnmodifiedSince,
      session,
      responseString,
      responseStatus
    )

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

  genericCreateTests()

  genericUpdateTests()
}
