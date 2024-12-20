package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.client.OrganisaatioClient
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.elasticsearch.HakuClient
import fi.oph.kouta.external.kouta.{
  CasKoutaClient,
  KoutaHakuRequest,
  KoutaResponse,
  OidResponse,
  UpdateResponse,
  UuidResponse
}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{AuthorizationRules, OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.oph.kouta.logging.Logging

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakuService extends HakuService(HakuClient, CasKoutaClient, OrganisaatioServiceImpl)

class HakuService(
    val hakuClient: HakuClient,
    val koutaClient: CasKoutaClient,
    val organisaatioService: OrganisaatioService
) extends RoleEntityAuthorizationService[Haku]
    with Logging {

  override val roleEntity: RoleEntity = Role.Haku

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    hakuClient
      .getHaku(oid).map(haku =>
        authorizeGet(
          haku,
          AuthorizationRules(
            roleEntity.readRoles.filterNot(_ == Indexer),
            allowAccessToParentOrganizations = true,
            additionalAuthorizedOrganisaatioOids =
              haku.hakukohteenLiittajaOrganisaatiot
          )
        )
      )

  def create(haku: Haku)(implicit authenticated: Authenticated): Future[KoutaResponse[HakuOid]] = {
    koutaClient.create("kouta-backend.haku", KoutaHakuRequest(authenticated, haku)).map {
      case Right(response: OidResponse)  => Right(HakuOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x)                       => Left(x)
    }
  }

  def update(haku: Haku, ifUnmodifiedSince: Instant)(implicit
      authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.update("kouta-backend.haku", KoutaHakuRequest(authenticated, haku), ifUnmodifiedSince)

  def findByOids(hakuOids: Set[HakuOid])(implicit authenticated: Authenticated): Future[Seq[Haku]] = {
    hakuClient.findByOids(hakuOids)
  }

  def search(
      ataruId: Option[String],
      tarjoajaOids: Set[OrganisaatioOid],
      vuosi: Option[Int] = None
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Haku]] =
    OrganisaatioClient
      .asyncGetAllChildOidsFlat(tarjoajaOids)
      .flatMap(oids => hakuClient.search(ataruId, oids, vuosi))

}
