package fi.oph.kouta.external.domain.enums

import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel("""    KoutaLightMassResult:
    |      type: array
    |      items:
    |        oneOf:
    |          - type: object
    |            description: Onnistunut luonti
    |            required:
    |              - operation
    |              - success
    |            properties:
    |              operation:
    |                const: CREATE
    |              success:
    |                type: boolean
    |                const: true
    |                description: Onnistuiko pyynnön käsittely
    |              externalId:
    |                type: string
    |                description: Pyynnössä annettu externalId
    |          - type: object
    |            description: Onnistunut päivitys
    |            required:
    |              - operation
    |              - success
    |            properties:
    |              operation:
    |                const: UPDATE
    |              success:
    |                type: boolean
    |                const: true
    |                description: Onnistuiko pyynnön käsittely
    |              externalId:
    |                type: string
    |                description: Pyynnössä annettu externalId
    |          - type: object
    |            description: Odottamaton virhe.
    |            required:
    |              - operation
    |              - success
    |              - exception
    |            properties:
    |              operation:
    |                const: CREATE OR UPDATE
    |              success:
    |                type: boolean
    |                const: false
    |                description: Onnistuiko pyynnön käsittely
    |              externalId:
    |                type: string
    |                description: Pyynnössä annettu externalId
    |              exception:
    |                type: string
    |                description: Tapahtuneen virheen tyyppi
    |                example: java.lang.IllegalArgumentException
    |""")
sealed abstract class KoutaLightMassResult(operation: Operation, success: Boolean)

object KoutaLightMassResult {
  case class UpdateSuccess(
      operation: Operation,
      success: Boolean,
      externalId: Option[String]
  ) extends KoutaLightMassResult(operation, success)

  object UpdateSuccess {
    def apply(externalId: Option[String]): UpdateSuccess =
      UpdateSuccess(Operation.Update, success = true, externalId)
  }

  case class CreateSuccess(operation: Operation, success: Boolean, externalId: Option[String])
      extends KoutaLightMassResult(operation, success)

  object CreateSuccess {
    def apply(externalId: Option[String]): CreateSuccess =
      CreateSuccess(Operation.Create, success = true, externalId = externalId)
  }

  case class Error(
      operation: Operation,
      success: Boolean,
      externalId: Option[String],
      exception: String
  ) extends KoutaLightMassResult(operation, success)

  object Error {
    def apply(operation: Operation, externalId: Option[String], exception: Throwable): Error =
      Error(operation, success = false, externalId, exception.toString)
  }
}
