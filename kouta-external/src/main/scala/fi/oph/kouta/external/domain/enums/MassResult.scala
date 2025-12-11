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
    |            required:
    |              - operation
    |              - success
    |              - oid
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
    |              externalId:
    |                type: string
    |                description: Pyynnössä annettu externalId
    |          - type: object
    |            description: Onnistunut päivitys
    |            required:
    |              - operation
    |              - success
    |              - updated
    |              - oid
    |            properties:
    |              operation:
    |                const: UPDATE
    |              success:
    |                type: boolean
    |                const: true
    |                description: Onnistuiko pyynnön käsittely
    |              oid:
    |                type: string
    |                description: Pyynnössä annettu yksilöivä tunniste
    |                example: 1.2.246.562.13.00000000000000000009
    |              externalId:
    |                type: string
    |                description: Pyynnössä annettu externalId
    |              updated:
    |                type: boolean
    |                description: Oliko objektissa päivitettävää
    |                example: true
    |          - type: object
    |            description: Virhe objektia talletettaessa. Objektia ei ole lisätty / päivitetty.
    |            required:
    |              - operation
    |              - success
    |              - status
    |              - message
    |            properties:
    |              operation:
    |                enum: [CREATE, UPDATE]
    |              success:
    |                type: boolean
    |                const: false
    |                description: Onnistuiko pyynnön käsittely
    |              oid:
    |                type: string
    |                description: Pyynnössä annettu yksilöivä tunniste
    |                example: 1.2.246.562.13.00000000000000000009
    |              externalId:
    |                type: string
    |                description: Pyynnössä annettu externalId
    |              status:
    |                type: int
    |                description: HTTP-vastauskoodi loppupalvelimelta
    |                example: 403
    |              message:
    |                type: string
    |                description: Vastauksen sisältö loppupalvelimelta
    |          - type: object
    |            description: Odottamaton virhe. Objekti voi olla talletettu tai sitten ei.
    |            required:
    |              - operation
    |              - success
    |              - exception
    |            properties:
    |              operation:
    |                enum: [CREATE, UPDATE]
    |              success:
    |                type: boolean
    |                const: false
    |                description: Onnistuiko pyynnön käsittely
    |              oid:
    |                type: string
    |                description: Pyynnössä annettu yksilöivä tunniste
    |                example: 1.2.246.562.13.00000000000000000009
    |              externalId:
    |                type: string
    |                description: Pyynnössä annettu externalId
    |              exception:
    |                type: string
    |                description: Tapahtuneen virheen tyyppi
    |                example: java.lang.IllegalArgumentException
    |""")
sealed abstract class MassResult(operation: Operation, success: Boolean)

object MassResult {
  case class UpdateSuccess(
      operation: Operation,
      success: Boolean,
      oid: Oid,
      externalId: Option[String],
      updated: Boolean
  ) extends MassResult(operation, success)

  object UpdateSuccess {
    def apply(oid: Oid, externalId: Option[String], updated: Boolean): UpdateSuccess =
      UpdateSuccess(Operation.Update, success = true, oid, externalId, updated = updated)
  }

  case class CreateSuccess(operation: Operation, success: Boolean, oid: Oid, externalId: Option[String])
      extends MassResult(operation, success)

  object CreateSuccess {
    def apply(oid: Oid, externalId: Option[String]): CreateSuccess =
      CreateSuccess(Operation.Create, success = true, oid = oid, externalId = externalId)
  }

  case class Failure(
      operation: Operation,
      success: Boolean,
      oid: Option[Oid],
      externalId: Option[String],
      status: Int,
      message: String
  ) extends MassResult(operation, success)

  object Failure {
    def apply(operation: Operation, oid: Option[Oid], externalId: Option[String], status: Int, message: String): Failure =
      Failure(operation, success = false, oid, externalId, status, message)
  }

  case class Error(operation: Operation, success: Boolean, oid: Option[Oid], externalId: Option[String], exception: String)
      extends MassResult(operation, success)

  object Error {
    def apply(operation: Operation, oid: Option[Oid], externalId: Option[String], exception: Throwable): Error =
      Error(operation, success = false, oid, externalId, exception.getClass.getCanonicalName)
  }
}
