package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, HakukohderyhmaOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.HakukohdeClient
import fi.oph.kouta.external.kouta._
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{
  AuthorizationRules,
  OrganisaatioService,
  OrganizationAuthorizationFailedException,
  RoleEntityAuthorizationService
}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object HakukohdeService
    extends HakukohdeService(
      HakukohdeClient,
      HakukohderyhmaService,
      CasKoutaClient,
      OrganisaatioServiceImpl,
      HakuService
    )

class HakukohdeService(
    hakukohdeClient: HakukohdeClient,
    hakukohderyhmaService: HakukohderyhmaService,
    koutaClient: CasKoutaClient,
    val organisaatioService: OrganisaatioService,
    hakuService: HakuService
) extends RoleEntityAuthorizationService[Hakukohde]
    with Logging {

  def executor: ExecutionContext      = global
  override val roleEntity: RoleEntity = Role.Hakukohde

  def getHakukohdeAuthorizeByHakukohderyhma(
      oid: HakukohdeOid
  )(implicit authenticated: Authenticated): Future[Hakukohde] = {
    hakukohdeClient.getHakukohde(oid).flatMap { case (hakukohde, _) =>
      hakukohderyhmaService.authorizeHakukohde(oid, hakukohde).flatMap { hakukohde: Hakukohde =>
        Future.successful(hakukohde)
      }
    }
  }

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] = {
    hakukohdeClient
      .getHakukohde(oid)
      .flatMap { case (hakukohde, tarjoajat) =>
        hakukohderyhmaService
          .getHakukohderyhmatByHakukohdeOid(oid)
          .map(oids => (hakukohde.withHakukohderyhmat(oids), tarjoajat))
      }
      .map { case (hakukohde, tarjoajat) =>
        val rules = AuthorizationRules(
          roleEntity.readRoles.filterNot(_ == Indexer),
          additionalAuthorizedOrganisaatioOids = tarjoajat
        )
        authorizeGet(hakukohde, rules)
      }
  }

  private def hakukohteetWithHakukohderyhmat(
      oids: Set[HakukohdeOid],
      hakukohteet: Future[Seq[Hakukohde]]
  ): Future[Seq[Hakukohde]] = {
    val fetchedHakukohderyhmat: Future[Map[HakukohdeOid, Seq[HakukohderyhmaOid]]] = Future
      .sequence(
        oids.map(oid => hakukohderyhmaService.getHakukohderyhmatByHakukohdeOid(oid).map(hoids => Map(oid -> hoids)))
      )
      .map(_.reduce(_ ++ _))

    hakukohteet.flatMap(hk =>
      fetchedHakukohderyhmat.map(mk => hk.map(h => h.withHakukohderyhmat(h.oid.flatMap(mk.get).getOrElse(Seq.empty))))
    )
  }

  def doSearch(
      hakuOid: Option[HakuOid],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      q: Option[String],
      all: Boolean,
      withHakukohderyhmat: Boolean,
      hakukohdeOids: Option[Set[HakukohdeOid]] = None
  ): Future[Seq[Hakukohde]] = {
    val tarjoajaOidsWithChilds = Some(
      tarjoajaOids.getOrElse(Set()).flatMap(organisaatioService.getAllChildOidsFlat(_, false))
    )
    val hakukohteet: Future[Seq[Hakukohde]] =
      hakukohdeClient.search(hakuOid, if (all) None else tarjoajaOidsWithChilds, q, hakukohdeOids)

    if (withHakukohderyhmat) {
      if (hakukohdeOids.isDefined) {
        hakukohteetWithHakukohderyhmat(hakukohdeOids.getOrElse(Set()), hakukohteet)
      } else {
        hakukohteet.flatMap(hk =>
          hakukohteetWithHakukohderyhmat(
            hk.flatMap(_.oid).toSet,
            Future.successful(hk)
          )
        )
      }
    } else {
      hakukohteet
    }
  }

  def search(
      hakuOid: Option[HakuOid],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      q: Option[String],
      all: Boolean,
      withHakukohderyhmat: Boolean
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    checkHakuExists.flatMap(_ => doSearch(hakuOid, tarjoajaOids, q, all, withHakukohderyhmat, None))
  }

  def searchAuthorizeByHakukohderyhma(
      hakuOid: Option[HakuOid],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      q: Option[String],
      all: Boolean,
      withHakukohderyhmat: Boolean
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val authorizedHakukohderyhmaOids: Set[HakukohderyhmaOid] = hakukohderyhmaService.getAuthorizedHakukohderyhmaOids

    Future
      .sequence(authorizedHakukohderyhmaOids.map(hakukohderyhmaService.getHakukohteetByHakukohderyhmaOid(_)))
      .map(_.flatten)
      .flatMap {
        case oids if oids.nonEmpty => doSearch(hakuOid, tarjoajaOids, q, all, withHakukohderyhmat, Some(oids))
        case _ =>
          val errorString: String = s"User missing rights to search hakukohteet via hakukohderyhma roles."
          throw new OrganizationAuthorizationFailedException(errorString)
      }
  }

  def create(hakukohde: Hakukohde)(implicit authenticated: Authenticated): Future[KoutaResponse[HakukohdeOid]] = {
    koutaClient.create("kouta-backend.hakukohde", KoutaHakukohdeRequest(authenticated, hakukohde)).map {
      case Right(response: OidResponse)  => Right(HakukohdeOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x)                       => Left(x)
    }
  }

  def update(hakukohde: Hakukohde, ifUnmodifiedSince: Instant)(implicit
      authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.update("kouta-backend.hakukohde", KoutaHakukohdeRequest(authenticated, hakukohde), ifUnmodifiedSince)

}
