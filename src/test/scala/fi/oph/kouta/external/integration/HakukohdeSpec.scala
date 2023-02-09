package fi.oph.kouta.external.integration

import java.util.UUID
import fi.oph.kouta.TestOids._
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, HakukohderyhmaOid, KoulutusOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.external.KoutaBackendMock
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.integration.fixture._
import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.security.{CasSession, Role}

import java.time.Instant

class HakukohdeSpec
    extends HakukohdeFixture
    with AccessControlSpec
    with GenericCreateTests[Hakukohde]
    with GenericUpdateTests[Hakukohde]
    with KoutaBackendMock {

  override val roleEntities       = Seq(Role.Hakukohde)
  val getPath: String             = HakukohdePath
  val entityName: String          = "hakukohde"
  val index: String               = s"$entityName-kouta"
  val existingId: HakukohdeOid    = HakukohdeOid("1.2.246.562.20.00000000000000000001")
  val nonExistingId: HakukohdeOid = HakukohdeOid("1.2.246.562.20.00000000000000000000")

  val hakuOid: HakuOid         = HakuOid("1.2.246.562.29.00000000000000000001")
  val toteutusId: ToteutusOid  = ToteutusOid("1.2.246.562.17.00000000000000000001")
  val koulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000001")
  val valintaperusteId: UUID   = UUID.fromString("fa7fcb96-3f80-4162-8d19-5b74731cf90c")
  val sorakuvausId: UUID       = UUID.fromString("e17773b2-f5a0-418d-a49f-34578c4b3625")

  val toteutusWithTarjoajaOid: ToteutusOid         = ToteutusOid("1.2.246.562.17.00000000000000000002")
  val hakukohdeWithTarjoajaOid: HakukohdeOid       = HakukohdeOid("1.2.246.562.20.00000000000000000002")
  val hakukohdeWithHakukohderyhmaOid: HakukohdeOid = HakukohdeOid("1.2.246.562.20.00000000000000000003")
  val testiHakukohderyhmaOid: HakukohderyhmaOid    = HakukohderyhmaOid("1.2.246.562.28.00000000000000000015")
  override val createdOid                          = "1.2.246.562.20.123456789"
  override val updatedOidBase                      = "1.2.246.562.20.1"

  def mockCreate(
      organisaatioOid: OrganisaatioOid,
      responseString: String,
      responseStatus: Int = 200,
      session: Option[(UUID, CasSession)] = None
  ): Unit =
    addCreateMock(
      entityName,
      KoutaBackendConverters.convertHakukohde(hakukohde(organisaatioOid)),
      "kouta-backend.hakukohde",
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
      KoutaBackendConverters.convertHakukohde(hakukohde(oidOrId)),
      "kouta-backend.hakukohde",
      ifUnmodifiedSince,
      session,
      responseString,
      responseStatus
    )

  s"GET /hakukohde/:id" should s"get hakukohde from elastic search" in {
    get(existingId, defaultSessionId)
  }

  it should s"have ${KoutaServlet.LastModifiedHeader} header in the response" in {
    get(s"$getPath/$existingId", headers = Seq(defaultSessionHeader)) {
      status should equal(200)
      header.get(KoutaServlet.LastModifiedHeader) should not be empty
      KoutaServlet.parseHttpDate(header(KoutaServlet.LastModifiedHeader)).toOption should not be empty
    }
  }

  it should s"return 404 if $entityName not found" in {
    get(s"$getPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id $nonExistingId from $index")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$getPath/$nonExistingId") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should s"allow a user of the $entityName organization to read the $entityName" in {
    get(existingId, crudSessionIds(ChildOid))
  }

  it should s"deny a user without access to the $entityName organization" in {
    get(existingId, crudSessionIds(EvilChildOid), 403)
  }

  it should s"allow a user of an ancestor organization to read the $entityName" in {
    get(existingId, crudSessionIds(ParentOid))
  }

  it should s"deny a user with only access to a descendant organization" in {
    get(existingId, crudSessionIds(GrandChildOid), 403)
  }

  it should "deny a user with the wrong role" in {
    get(existingId, otherRoleSessionId, 403)
  }

  it should "deny indexer access" in {
    get(existingId, indexerSessionId, 403)
  }

  it should "allow the user of a tarjoaja organization of the toteutus" in {
    get(hakukohdeWithTarjoajaOid, readSessionIds(ChildOid))
  }

  it should "allow the user of an ancestor of a tarjoaja organization of the toteutus" in {
    get(hakukohdeWithTarjoajaOid, crudSessionIds(ParentOid))
  }

  it should "deny the user of a descendant of a tarjoaja organization of the toteutus" in {
    get(hakukohdeWithTarjoajaOid, crudSessionIds(GrandChildOid), 403)
  }

  it should "deny the user without rights to hakukohde or hakukohderyhma" in {
    get(hakukohdeWithHakukohderyhmaOid, crudSessionIds(GrandChildOid), 403)
  }

  it should "allow the user with rights to hakukohderyhma" in {
    get(hakukohdeWithHakukohderyhmaOid, hakukohderyhmaCrudSessionIds(testiHakukohderyhmaOid))
  }

  it should "deny the user to search without rights to haku" in {
    search(Some(hakuOid), None, None, true, otherRoleSessionId, 403)
  }

  it should "allow the user with pääkäyttäjä rights to search" in {
    search(Some(hakuOid), None, None, true, ophPaakayttajaSessionId)
  }
  it should "allow the user with hakukohderyhmä rights to search" in {
    search(Some(hakuOid), None, None, true, hakukohderyhmaCrudSessionIds(searchHakukohderyhmaOid))
  }

  genericCreateTests()

  genericUpdateTests()
}
