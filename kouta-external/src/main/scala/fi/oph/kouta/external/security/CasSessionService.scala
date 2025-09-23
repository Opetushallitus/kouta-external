package fi.oph.kouta.external.security

import java.util.UUID
import java.util.concurrent.TimeUnit

import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.kouta.CallerId
import fi.oph.kouta.external.client.KayttooikeusClient
import fi.oph.kouta.external.database.SessionDAO
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import fi.oph.kouta.security.{CasSession, ServiceTicket, Session}
import fi.oph.kouta.logging.Logging
import scalaz.concurrent.Task

import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.util.control.NonFatal

object CasSessionService
    extends CasSessionService(
      ProductionSecurityContext(KoutaConfigurationFactory.configuration.securityConfiguration),
      KayttooikeusClient
    )

abstract class CasSessionService(val securityContext: SecurityContext, val userDetailsService: KayttooikeusClient)
    extends Logging with CallerId {
  logger.info(s"Using security context ${securityContext.getClass.getSimpleName}")

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val serviceIdentifier: String = securityContext.casServiceIdentifier
  val casUrl: String            = securityContext.casUrl

  private val casClient = securityContext.casClient

  private def validateServiceTicket(ticket: ServiceTicket): Either[Throwable, String] = {
    val ServiceTicket(s) = ticket
    val result = toScala(casClient.validateServiceTicketWithVirkailijaUsername(securityContext.casServiceIdentifier, s))
    try {
      Right(Await.result(result, Duration(30, TimeUnit.SECONDS)))
    } catch {
      case NonFatal(t) =>
        logger.debug("Ticket validation error", t)
        Left(new AuthenticationFailedException(s"Failed to validate service ticket $s", t))
      case t: Throwable =>
        Left(t)
    }
  }

  private def storeSession(ticket: ServiceTicket, user: KayttooikeusUserDetails): (UUID, CasSession) = {
    val session = CasSession(ticket, user.oid, user.authorities)
    logger.debug(s"Storing to session: ${session.casTicket} ${session.personOid} ${session.authorities}")
    val id = SessionDAO.store(session)
    (id, session)
  }

  private def createSession(ticket: ServiceTicket): Either[Throwable, (UUID, CasSession)] = {
    validateServiceTicket(ticket)
      .map(userDetailsService.getUserByUsername)
      .map(storeSession(ticket, _))
  }

  private def getSession(id: UUID): Either[Throwable, (UUID, Session)] =
    SessionDAO
      .get(id)
      .map(session => (id, session))
      .toRight(new AuthenticationFailedException(s"Session $id doesn't exist"))

  def getSession(ticket: Option[ServiceTicket], id: Option[UUID]): Either[Throwable, (UUID, Session)] = {
    logger.trace(s"Getting session with ticket $ticket and session id $id")
    (ticket, id) match {
      case (None, None) =>
        logger.trace("No session found")
        Left(new AuthenticationFailedException("No credentials given"))
      case (None, Some(i)) => getSession(i)
      case (Some(t), None) => createSession(t)
      case (Some(t), Some(i)) =>
        getSession(i).left.flatMap {
          case _: AuthenticationFailedException => createSession(t)
          case e                                => Left(e)
        }
    }
  }

  def deleteSession(ticket: ServiceTicket): Boolean = SessionDAO.delete(ticket)
}
