package fi.oph.kouta.external.domain.enums

import fi.oph.kouta.domain.oid.Oid
import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    MassResult:
    |      type: array
    |      items:
    |        oneOf:
    |          - type: object
    |            description: Onnistunut luonti
    |            properties:
    |              operation:
    |                const: CREATE
    |              success:
    |                type: boolean
    |                const: true
    |                description: Onnistuiko pyynnön käsittely
    |              oid:
    |                type: string
    |                description: Uuden objektin yksilöivä oid
    |                example: 1.2.246.562.13.00000000000000000009
    |          - type: object
    |            description: Onnistunut päivitys
    |            properties:
    |              operation:
    |                const: UPDATE
    |              success:
    |                type: boolean
    |                const: true
    |                description: Onnistuiko pyynnön käsittely
    |              updated:
    |                type: boolean
    |                description: Oliko objektissa päivitettävää
    |                example: true
    |          - type: object
    |            description: Virhe objektia talletettaessa. Objektia ei ole lisätty / päivitetty.
    |            properties:
    |              operation:
    |                enum: [CREATE, UPDATE]
    |              success:
    |                type: boolean
    |                const: false
    |                description: Onnistuiko pyynnön käsittely
    |              status:
    |                type: int
    |                description: HTTP-vastauskoodi loppupalvelimelta
    |                example: 403
    |              message:
    |                type: string
    |                description: Vastauksen sisältö loppupalvelimelta
    |          - type: object
    |            description: Odottamaton virhe. Objekti voi olla talletettu tai sitten ei.
    |            properties:
    |              operation:
    |                enum: [CREATE, UPDATE]
    |              success:
    |                type: boolean
    |                const: false
    |                description: Onnistuiko pyynnön käsittely
    |              exception:
    |                type: string
    |                description: Tapahtuneen virheen tyyppi
    |                example: java.lang.IllegalArgumentException
    |""")
sealed abstract class MassResult(operation: Operation, success: Boolean)

object MassResult {
  case class UpdateSuccess(operation: Operation, success: Boolean, updated: Boolean)
      extends MassResult(operation, success)

  object UpdateSuccess {
    def apply(updated: Boolean): UpdateSuccess = UpdateSuccess(Operation.Update, success = true, updated = updated)
  }

  case class CreateSuccess(operation: Operation, success: Boolean, oid: Oid)
      extends MassResult(operation, success)

  object CreateSuccess {
    def apply(oid: Oid): CreateSuccess = CreateSuccess(Operation.Create, success = true, oid = oid)
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
