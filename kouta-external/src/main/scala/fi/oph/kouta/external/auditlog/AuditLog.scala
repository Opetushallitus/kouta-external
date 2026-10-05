package fi.oph.kouta.external.auditlog

import com.google.gson.JsonParser
import fi.oph.kouta.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.kouta._
import fi.oph.kouta.security.{ServiceTicket, Session}
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.auditlog._
import jakarta.servlet.http.HttpServletRequest
import org.ietf.jgss.Oid
import org.slf4j.LoggerFactory

import java.net.InetAddress
import java.time.Instant
import java.util.UUID
import scala.util.control.NonFatal

object AuditLogger extends Logger {
  private val logger = LoggerFactory.getLogger(classOf[Audit])

  override def log(msg: String): Unit = logger.info(msg)
}

object AuditLog extends AuditLog(AuditLogger)

class AuditLog(val logger: Logger) extends GsonSupport {

  val audit = new Audit(logger, "kouta-external", ApplicationType.VIRKAILIJA)

  private val TargetEpaselva      = "Tuntematon tai muutosten implikoima kohde"
  private val UserAgentTuntematon = "Tuntematon"

  private val appLogger = LoggerFactory.getLogger(classOf[AuditLog])

  def init(): Unit = {}

  /**
   * Audit-lokitus operaatiolle, jonka muutos on jo tehty eikä sitä voi perua (esim. kouta-backendiin
   * välitetty tallennus). Lokituksen epäonnistuminen kirjataan sovelluslokiin, eikä se kaada pyyntöä.
   */
  def logAfterCommit(description: => String)(log: => Unit): Unit =
    try log
    catch {
      case NonFatal(e) => appLogger.error(s"Audit-lokitus epäonnistui: $description", e)
    }

  def logLogin(sessionId: UUID, session: Session, ticket: ServiceTicket)(implicit request: HttpServletRequest): Unit = {
    val target = new Target.Builder().setField("personOid", session.personOid).build()
    // Kirjautuminen ei saa kaatua puuttuvaan User-Agent-otsakkeeseen, joten Authenticated-luokkaa ei käytetä tässä
    val userAgent = Option(request.getHeader("User-Agent")).getOrElse(UserAgentTuntematon)
    val ip        = InetAddress.getByName(Authenticated.getRemoteAddress(request))
    val user      = getUser(sessionId.toString, session, userAgent, ip)
    val changes   = new Changes.Builder().added("ticket", ticket.s).build()
    audit.log(user, AuditOperation.Login, target, changes)
  }

  def logCreate(request: ExternalRequest, response: IdResponse): Unit = {
    val entity   = requestEntity(request)
    val resource = AuditResource(entity)
    val id = response match {
      case OidResponse(oid) => oid.s
      case UuidResponse(id) => id.toString
    }
    val changes = new Changes.Builder().added(toGson(entity).getAsJsonObject).build()
    audit.log(getUser(request.authenticated), resource.Create, getTarget(resource, Some(id)).build(), changes)
  }

  /**
   * Kouta-external ei tiedä päivitettävän entiteetin aiempaa tilaa, joten muutoksina logitetaan
   * kouta-backendille lähetetty entiteetti kokonaisuudessaan. Varsinainen muutos (diff) löytyy
   * kouta-backendin audit-lokista.
   */
  def logUpdate(request: ExternalRequest, response: UpdateResponse): Unit = {
    val entity   = requestEntity(request)
    val resource = AuditResource(entity)
    val target = getTarget(resource, requestEntityId(request))
      .setField("updated", response.updated.toString)
    val changes = new Changes.Builder().added(toGson(entity).getAsJsonObject).build()
    audit.log(getUser(request.authenticated), resource.Update, target.build(), changes)
  }

  def logKoutaLightCreate(externalId: String, ownerOrg: OrganisaatioOid, after: String)(implicit
      authenticated: Authenticated
  ): Unit = {
    val resource = AuditResource.KoutaLightKoulutus
    val target   = getTarget(resource, Some(externalId)).setField("ownerOrg", ownerOrg.s)
    val changes  = new Changes.Builder().added(JsonParser.parseString(after).getAsJsonObject).build()
    audit.log(getUser(authenticated), resource.Create, target.build(), changes)
  }

  def logKoutaLightUpdate(externalId: String, ownerOrg: OrganisaatioOid, before: String, after: String)(implicit
      authenticated: Authenticated
  ): Unit = {
    val resource = AuditResource.KoutaLightKoulutus
    val target   = getTarget(resource, Some(externalId)).setField("ownerOrg", ownerOrg.s)
    val changes  = ChangeFactory.getChanges(JsonParser.parseString(before), JsonParser.parseString(after))
    audit.log(getUser(authenticated), resource.Update, target.build(), changes)
  }

  def logKoutaLightSiirtotiedosto(
      startTime: Option[Instant],
      endTime: Instant,
      s3ObjectKeys: Seq[String],
      count: Int
  )(implicit authenticated: Authenticated): Unit = {
    val target = new Target.Builder()
      .setField("type", AuditResource.KoutaLightKoulutus.name)
      .setField("endTime", endTime.toString)
    startTime.foreach(t => target.setField("startTime", t.toString))
    val changes = new Changes.Builder()
      .added("s3ObjectKeys", s3ObjectKeys.mkString(", "))
      .added("count", count.toString)
      .build()
    audit.log(getUser(authenticated), AuditOperation.KoutaLightSiirtotiedostoCreate, target.build(), changes)
  }

  def getUser(authenticated: Authenticated): User =
    getUser(authenticated.id, authenticated.session, authenticated.userAgent, authenticated.ip)

  def getUser(sessionId: String, session: Session, userAgent: String, ip: InetAddress): User =
    new User(new Oid(session.personOid), ip, sessionId, userAgent)

  private def getTarget(resource: AuditResource, targetId: Option[String]): Target.Builder =
    new Target.Builder()
      .setField("type", resource.name)
      .setField(resource.idField, targetId.getOrElse(TargetEpaselva))

  private def requestEntity(request: ExternalRequest): AnyRef =
    request match {
      case r: KoutaKoulutusRequest       => r.koulutus
      case r: KoutaToteutusRequest       => r.toteutus
      case r: KoutaHakuRequest           => r.haku
      case r: KoutaHakukohdeRequest      => r.hakukohde
      case r: KoutaValintaperusteRequest => r.valintaperuste
      case r: KoutaSorakuvausRequest     => r.sorakuvaus
    }

  private def requestEntityId(request: ExternalRequest): Option[String] =
    request match {
      case r: KoutaKoulutusRequest       => r.koulutus.oid.map(_.s)
      case r: KoutaToteutusRequest       => r.toteutus.oid.map(_.s)
      case r: KoutaHakuRequest           => r.haku.oid.map(_.s)
      case r: KoutaHakukohdeRequest      => r.hakukohde.oid.map(_.s)
      case r: KoutaValintaperusteRequest => r.valintaperuste.id.map(_.toString)
      case r: KoutaSorakuvausRequest     => r.sorakuvaus.id.map(_.toString)
    }
}
