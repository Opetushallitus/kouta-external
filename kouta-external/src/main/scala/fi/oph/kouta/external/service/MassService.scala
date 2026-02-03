package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.Oid
import fi.oph.kouta.external.domain.PerustiedotWithOid
import fi.oph.kouta.external.domain.enums.{MassResult, Operation}
import fi.oph.kouta.external.kouta.{KoutaResponse, UpdateResponse}
import fi.oph.kouta.external.service.MassService.tomorrowNoon
import fi.oph.kouta.external.servlet.MassOperations
import fi.oph.kouta.logging.Logging
import fi.oph.kouta.servlet.Authenticated
import fi.oph.kouta.util.Timer

import java.time.{Instant, LocalDateTime, LocalTime, ZoneOffset}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

trait MassService[ID <: Oid, T <: PerustiedotWithOid[ID, T]] {
  this: Logging =>

  def create(entity: T)(implicit authenticated: Authenticated): Future[KoutaResponse[ID]]

  def update(entity: T, ifUnmodifiedSince: Instant)(implicit
      authenticated: Authenticated
  ): Future[KoutaResponse[UpdateResponse]]

  def entityName: String

  def massImport(entities: List[T])(implicit authenticated: Authenticated): Future[List[MassResult]] = {
    val duplicateOids = entities.flatMap(_.oid).groupBy(identity).collect { case (x, List(_, _, _*)) => x }
    if (duplicateOids.nonEmpty) {
      return Future.failed(DuplicateOidException(duplicateOids))
    }
    implicit val executor: ExecutionContextExecutor = MassOperations.executor
    Future.traverse(entities)(k => Future(handleEntityInMass(k)))
  }

  private def handleEntityInMass(entity: T)(implicit authenticated: Authenticated): MassResult =
    Timer.timed(s"Handling $entityName with oid ${entity.oid}") {
      logger.info(s"Processing $entityName: ${entity.oid}")
      entity.oid match {
        case None      => handleCreate(entity)
        case Some(oid) => handleUpdate(oid, entity)
      }
    }

  private def handleCreate(entity: T)(implicit authenticated: Authenticated) =
    Try(createBlocking(entity)) match {
      case Failure(e) =>
        logger.error(s"Mass create on $entityName threw an exception. Entity = $entity", e)
        MassResult.Error(Operation.Create, entity.oid, entity.externalId, e)
      case Success(Left((status, message))) =>
        logger.warn(s"Creating $entityName failed. Response status = $status, message = $message. Entity = $entity")
        MassResult.Failure(Operation.Create, entity.oid, entity.externalId, status, message)
      case Success(Right(oid: ID)) =>
        logger.info(s"Created $entityName $oid")
        MassResult.CreateSuccess(oid, entity.externalId)
    }

  private def handleUpdate(oid: ID, entity: T)(implicit authenticated: Authenticated) =
    Try(updateBlocking(entity, tomorrowNoon())) match {
      case Failure(e) =>
        logger.error(s"Mass update on $entityName threw an exception. Entity = $entity", e)
        MassResult.Error(Operation.Update, entity.oid, entity.externalId, e)
      case Success(Left((status, message))) =>
        logger.warn(s"Updating $entityName failed. Response status = $status, message = $message. Entity = $entity")
        MassResult.Failure(Operation.Update, entity.oid, entity.externalId, status, message)
      case Success(Right(response)) =>
        logger.info(s"Updated $entityName $oid")
        MassResult.UpdateSuccess(oid, entity.externalId, response.updated)
    }

  private def createBlocking(entity: T)(implicit authenticated: Authenticated): KoutaResponse[ID] =
    Await.result(create(entity), 60.seconds)

  private def updateBlocking(entity: T, ifUnmodifiedSince: Instant)(implicit
      authenticated: Authenticated
  ): KoutaResponse[UpdateResponse] =
    Await.result(update(entity, ifUnmodifiedSince), 60.seconds)
}

object MassService {
  def tomorrowNoon(): Instant =
    LocalDateTime.now().plusDays(1).`with`(LocalTime.of(12, 0, 0)).toInstant(ZoneOffset.UTC)
}

case class DuplicateOidException(duplicates: Iterable[Oid])
    extends IllegalArgumentException(s"Pyynnössä oli monta kohdetta, joilla oli sama OID: ${duplicates.mkString(", ")}")
