package fi.oph.kouta.external.service

import fi.oph.kouta.domain.Julkaisutila
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

case class HakukohdeSearchParams(
    hakuOid: Option[HakuOid] = None,
    tarjoajaOids: Option[Set[OrganisaatioOid]] = None,
    q: Option[String] = None,
    all: Boolean = false,
    withHakukohderyhmat: Boolean = false,
    johtaaTutkintoon: Option[Boolean] = None,
    tila: Option[Set[Julkaisutila]] = None,
    hakutapa: Option[Set[String]] = None,
    opetuskieli: Option[Set[String]] = None,
    alkamiskausi: Option[String] = None,
    alkamisvuosi: Option[String] = None,
    koulutusaste: Option[Set[String]] = None
) {
  private def toMultiParamString[X <: Object](value: Option[Set[X]], key: String) =
    value.map(vs => vs.map(v => s"$key=${v.toString}").mkString("&"))

  def toQueryString(): String = {
    "?" + List(
      hakuOid.map(h => s"haku=${h.toString}"),
      toMultiParamString[OrganisaatioOid](tarjoajaOids, "tarjoaja"),
      q.map(q => s"q=$q"),
      Some(s"all=${all.toString}"),
      Some(s"withHakukohderyhmat=${withHakukohderyhmat}"),
      johtaaTutkintoon.map(jt => s"johtaaTutkintoon=${jt.toString}"),
      toMultiParamString(tila, "tila"),
      toMultiParamString(hakutapa, "hakutapa"),
      toMultiParamString(opetuskieli, "opetuskieli"),
      alkamiskausi.map(ak => s"alkamiskausi=${ak}"),
      alkamisvuosi.map(av => s"alkamisvuosi=${av}"),
      toMultiParamString(koulutusaste, "koulutusaste")
    ).flatten.mkString("&")
  }
}

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
        oids.map(oid => hakukohderyhmaService.getHakukohderyhmatByHakukohdeOid(oid).map((oid, _)))
      )
      .map(_.toMap)

    hakukohteet.flatMap(hk =>
      fetchedHakukohderyhmat.map(mk => hk.map(h => h.withHakukohderyhmat(h.oid.flatMap(mk.get).getOrElse(Seq.empty))))
    )
  }

  def doSearch(
      searchParams: HakukohdeSearchParams,
      hakukohdeOids: Option[Set[HakukohdeOid]] = None
  ): Future[Seq[Hakukohde]] = {
    val tarjoajaOids        = searchParams.tarjoajaOids
    val withHakukohderyhmat = searchParams.withHakukohderyhmat

    val tarjoajaOidsWithChilds =
      if (searchParams.all) None
      else
        Some(
          tarjoajaOids.getOrElse(Set()).flatMap(organisaatioService.getAllChildOidsFlat(_, false))
        )
      val hakukohteet: Future[Seq[Hakukohde]] =
        hakukohdeClient.search(searchParams.copy(tarjoajaOids = tarjoajaOidsWithChilds), hakukohdeOids)
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
      searchParams: HakukohdeSearchParams
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists = searchParams.hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    checkHakuExists.flatMap(_ => doSearch(searchParams, None))
  }

  def searchAuthorizeByHakukohderyhma(
      searchParams: HakukohdeSearchParams
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {

    val authorizedHakukohderyhmaOids: Set[HakukohderyhmaOid] = hakukohderyhmaService.getAuthorizedHakukohderyhmaOids

    Future
      .sequence(authorizedHakukohderyhmaOids.map(hakukohderyhmaService.getHakukohteetByHakukohderyhmaOid(_)))
      .map(_.flatten)
      .flatMap {
        case oids if oids.nonEmpty => doSearch(searchParams, Some(oids))
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
