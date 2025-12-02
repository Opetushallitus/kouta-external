package fi.oph.kouta.external.service

import fi.oph.kouta.domain.Amm
import fi.oph.kouta.domain.oid.{GenericOid, KoulutusOid}
import fi.oph.kouta.external.domain.{AmmatillinenKoulutusMetadata, AmmatillinenOsaamisalaKoulutusMetadata, Koulutus}
import fi.oph.kouta.external.elasticsearch.{EPerusteClient, KoulutusClient}
import fi.oph.kouta.external.kouta._
import fi.oph.kouta.logging.Logging
import fi.oph.kouta.security.Role.Indexer
import fi.oph.kouta.security.{Role, RoleEntity}
import fi.oph.kouta.service.{AuthorizationRuleForReadJulkinen, AuthorizationRules, OrganisaatioService, RoleEntityAuthorizationService}
import fi.oph.kouta.servlet.Authenticated

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KoulutusService extends KoulutusService(KoulutusClient, EPerusteClient, CasKoutaClient, OrganisaatioServiceImpl)

class KoulutusService(
    koulutusClient: KoulutusClient,
    ePerusteClient: EPerusteClient,
    val koutaClient: CasKoutaClient,
    val organisaatioService: OrganisaatioService
) extends RoleEntityAuthorizationService[Koulutus]
    with Logging {

  override val roleEntity: RoleEntity = Role.Koulutus

  def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] = {
    koulutusClient.getKoulutus(oid).flatMap { koulutus =>
      val authorizedKoulutus: Koulutus = authorizeGet(
        koulutus,
        AuthorizationRules(
          requiredRoles = roleEntity.readRoles.filterNot(_ == Indexer),
          allowAccessToParentOrganizations = true,
          overridingAuthorizationRule = Some(AuthorizationRuleForReadJulkinen),
          additionalAuthorizedOrganisaatioOids = koulutus.tarjoajat
        )
      )

      authorizedKoulutus.ePerusteId match {
        case Some(ePerusteId) if authorizedKoulutus.koulutustyyppi == Amm =>
          ePerusteClient.getEPeruste(GenericOid(ePerusteId.toString)).map { eperuste =>
            val tyotehtavatJoissaVoiToimia = eperuste.tyotehtavatJoissaVoiToimia
            val suorittaneenOsaaminen      = eperuste.suorittaneenOsaaminen

            authorizedKoulutus.metadata match {
              case Some(metadata) =>
                metadata match {
                  case m: AmmatillinenKoulutusMetadata =>
                    authorizedKoulutus.copy(metadata =
                      Some(m.copy(kuvaus = tyotehtavatJoissaVoiToimia, osaamistavoitteet = suorittaneenOsaaminen))
                    )
                }
              case None => authorizedKoulutus
            }
          }
        case _ => Future(authorizedKoulutus)
      }
    }
  }

  def create(koulutus: Koulutus)(implicit authenticated: Authenticated): Future[KoutaResponse[KoulutusOid]] =
    koutaClient.create("kouta-backend.koulutus", KoutaKoulutusRequest(authenticated, koulutus)).map {
      case Right(response: OidResponse) =>
        Right(KoulutusOid(response.oid.s))
      case Right(response: UuidResponse) => Left((200, response.id.toString))
      case Left(x) =>
        Left(x)
    }

  def update(koulutus: Koulutus, ifUnmodifiedSince: Instant)(implicit
      authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]] =
    koutaClient.update("kouta-backend.koulutus", KoutaKoulutusRequest(authenticated, koulutus), ifUnmodifiedSince)
}
