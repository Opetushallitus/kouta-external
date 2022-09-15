package fi.oph.kouta.external.integration

import fi.oph.kouta.TestOids.{ChildOid, EvilChildOid, ParentOid}
import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.security.{CasSession, Role}

import java.time.Instant
import java.util.UUID

class HakuSpec
    extends HakuFixture
    with AccessControlSpec
    with GenericGetTests[Haku, HakuOid]
    with GenericCreateTests[Haku]
    with GenericUpdateTests[Haku]
    with KoutaBackendMock {

  override val roleEntities       = Seq(Role.Haku)
  override val entityPath: String = HakuPath
  override val entityName         = "haku"
  val existingId: HakuOid         = HakuOid("1.2.246.562.29.00000000000000000001")
  val nonExistingId: HakuOid      = HakuOid("1.2.246.562.29.00000000000000000000")
  override val createdOid         = "1.2.246.562.29.123456789"
  override val updatedOidBase     = "1.2.246.562.29.1"

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      entityName,
      haku(organisaatioOid),
      "kouta-backend.haku",
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
      haku(oidOrId),
      "kouta-backend.haku",
      ifUnmodifiedSince,
      session,
      responseString,
      responseStatus
    )

  getTests()

  genericCreateTests()

  genericUpdateTests()
}
