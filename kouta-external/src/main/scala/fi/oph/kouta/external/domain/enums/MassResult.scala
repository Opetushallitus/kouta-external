package fi.oph.kouta.external.domain.enums

import fi.oph.kouta.domain.oid.KoulutusOid

sealed abstract class MassResult(operation: Operation, success: Boolean)

object MassResult {
  case class UpdateSuccess(operation: Operation, success: Boolean, updated: Boolean)
      extends MassResult(operation, success)

  object UpdateSuccess {
    def apply(updated: Boolean): UpdateSuccess = UpdateSuccess(Operation.Update, success = true, updated = updated)
  }

  case class CreateSuccess(operation: Operation, success: Boolean, oid: KoulutusOid)
      extends MassResult(operation, success)

  object CreateSuccess {
    def apply(oid: KoulutusOid): CreateSuccess = CreateSuccess(Operation.Create, success = true, oid = oid)
  }

  case class Failure(operation: Operation, success: Boolean, status: Int, message: String)
      extends MassResult(operation, success)

  object Failure {
    def apply(operation: Operation, status: Int, message: String): Failure =
      Failure(operation, success = false, status, message)
  }

  case class Error(operation: Operation, success: Boolean, exception: String) extends MassResult(operation, success)

  object Error {
    def apply(operation: Operation, exception: Throwable): Error =
      Error(operation, success = false, exception.getClass.getCanonicalName)
  }
}
