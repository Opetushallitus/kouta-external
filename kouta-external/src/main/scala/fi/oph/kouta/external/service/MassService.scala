package fi.oph.kouta.external.service

import fi.oph.kouta.domain.oid.Oid
import fi.oph.kouta.external.domain.PerustiedotWithOid
import fi.oph.kouta.external.domain.enums.{MassResult, Operation}
import fi.oph.kouta.external.kouta.{KoutaResponse, UpdateResponse}
import fi.oph.kouta.external.servlet.MassOperations
import fi.oph.kouta.logging.Logging
import fi.oph.kouta.servlet.Authenticated
import fi.oph.kouta.util.Timer

import java.time.Instant
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

trait MassService[ID <: Oid, T <: PerustiedotWithOid[ID, T]] {
  this: Logging =>

  def create(entity: T)(implicit authenticated: Authenticated): Future[KoutaResponse[ID]]

  def update(entity: T, ifUnmodifiedSince: Instant)
            (implicit authenticated: Authenticated): Future[KoutaResponse[UpdateResponse]]

  def entityName: String

  def massImport(entities: List[T])
                (implicit authenticated: Authenticated): Future[List[MassResult]] = {
    implicit val executor: ExecutionContextExecutor = MassOperations.executor
    Future.traverse(entities)(k => Future(handleEntityInMass(k)))
  }

  private def handleEntityInMass(entity: T)(implicit authenticated: Authenticated): MassResult =
    Timer.timed(s"Handling $entityName with oid ${entity.oid}") {
      logger.info(s"Processing $entityName: ${entity.oid}")
      entity.oid match {
        case None => Try(createBlocking(entity)) match {
          case Failure(e) =>
            logger.error(s"Mass create on $entityName threw an exception. Entity = $entity", e)
            MassResult.Error(Operation.Create, e)
          case Success(Left((status, message))) =>
            logger.warn(s"Creating $entityName failed. Response status = $status, message = $message. Entity = $entity")
            MassResult.Failure(Operation.Create, status, message)
          case Success(Right(oid: ID)) =>
            logger.info(s"Created $entityName $oid")
            MassResult.CreateSuccess(oid)
        }
        case Some(oid) => Try(updateBlocking(entity, Instant.now())) match {
          case Failure(e) =>
            logger.error(s"Mass update on $entityName threw an exception. Entity = $entity", e)
            MassResult.Error(Operation.Update, e)
          case Success(Left((status, message))) =>
            logger.warn(s"Updating $entityName failed. Response status = $status, message = $message. Entity = $entity")
            MassResult.Failure(Operation.Update, status, message)
          case Success(Right(response)) =>
            logger.info(s"Updated $entityName $oid")
            MassResult.UpdateSuccess(response.updated)
        }
      }
    }

  private def createBlocking(entity: T)(implicit authenticated: Authenticated): KoutaResponse[ID] =
    Await.result(create(entity), 60.seconds)

  private def updateBlocking(entity: T, ifUnmodifiedSince: Instant)(implicit
                                                                              authenticated: Authenticated
  ): KoutaResponse[UpdateResponse] =
    Await.result(update(entity, ifUnmodifiedSince), 60.seconds)

}
