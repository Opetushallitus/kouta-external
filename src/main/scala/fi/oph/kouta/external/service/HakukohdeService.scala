package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, HakukohderyhmaOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.elasticsearch.{HakuClient, HakukohdeClient}
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{OrganisaatioService, OrganizationAuthorizationFailedException, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object HakukohdeService
    extends HakukohdeService(HakuClient,HakukohdeClient, HakukohderyhmaService, OrganisaatioServiceImpl, HakuService)

class HakukohdeService(
    hakuClient: HakuClient,
    hakukohdeClient: HakukohdeClient,
    hakukohderyhmaService: HakukohderyhmaService,
    val organisaatioService: OrganisaatioService,
    hakuService: HakuService
) extends RoleEntityAuthorizationService[Hakukohde]
    with Logging  {

  def executor: ExecutionContext      = global
  override val roleEntity: RoleEntity = Role.Hakukohde

  def getHakukohdeAuthorizeByHakukohderyhma(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] = {
    hakukohdeClient.getHakukohde(oid).flatMap {
      case (hakukohde, _) =>
        hakukohderyhmaService.authorizeHakukohde(oid, hakukohde).flatMap {
          hakukohde: Hakukohde => Future.successful(hakukohde)
        }
    }
  }

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] = {
    hakukohdeClient.getHakukohde(oid).map {
      case (hakukohde, tarjoajat) =>
        val rules = AuthorizationRules(roleEntity.readRoles.filterNot(_ == Indexer), additionalAuthorizedOrganisaatioOids = tarjoajat)
        authorizeGet(hakukohde, rules)
    }
  }

  def search(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String], all: Boolean)(
    implicit authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    val tarjoajaOidsWithChilds = Some(
      tarjoajaOids.getOrElse(Set()).flatMap(organisaatioService.getAllChildOidsFlat(_, false))
    )
    checkHakuExists.flatMap(_ => hakukohdeClient.search(hakuOid, if (all) None else tarjoajaOidsWithChilds, q, None))
  }

  def searchAuthorizeByHakukohderyhma(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String], all: Boolean)(
    implicit authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val authorizedHakukohderyhmaOids: Set[HakukohderyhmaOid] = hakukohderyhmaService.getAuthorizedHakukohderyhmaOids match {
      case s: Set[HakukohderyhmaOid] => s
      case _ =>
        val errorString: String = s"User missing rights to search via hakukohderyhma roles."
        logger.warn(errorString)
        throw new OrganizationAuthorizationFailedException(errorString)
    }

    Future.sequence(authorizedHakukohderyhmaOids.map { oid =>
      hakukohderyhmaService.getHakukohteetByHakukohderyhmaOid(oid)
    }
    ).map(_.flatten).flatMap {
      case oids if oids.nonEmpty =>
        val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuClient.getHaku(_).map(_ => ()))
        val tarjoajaOidsWithChilds = Some(
          tarjoajaOids.getOrElse(Set()).flatMap(organisaatioService.getAllChildOidsFlat(_, false))
        )
        checkHakuExists.flatMap(_ => hakukohdeClient.search(hakuOid, if (all) None else tarjoajaOidsWithChilds, q, Some(oids)))
      case _ => val errorString: String = s"User missing rights to search hakukohteet via hakukohderyhma roles."
        logger.warn(errorString)
        throw new OrganizationAuthorizationFailedException(errorString)
    }
  }
}
