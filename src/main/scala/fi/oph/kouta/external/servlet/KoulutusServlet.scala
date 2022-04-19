package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.service.KoulutusService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, FutureSupport, Ok}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object KoulutusServlet extends KoulutusServlet(KoulutusService)

class KoulutusServlet(koulutusService: KoulutusService)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/koulutus/{oid}",
    """    get:
      |      summary: Hae koulutus
      |      description: Hae koulutuksen tiedot annetulla koulutus-oidilla
      |      operationId: Hae koulutus
      |      tags:
      |        - Koulutus
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Koulutus-oid
      |          example: 1.2.246.562.13.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Koulutus'
      |""".stripMargin
  )
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    koulutusService.get(KoulutusOid(params("oid")))
      .map { koulutus =>
        Ok(koulutus, headers = Map(KoutaServlet.LastModifiedHeader -> createLastModifiedHeader(koulutus)))
      }
  }

  registerPath( "/koulutus/",
    """    put:
      |      summary: Tallenna uusi koulutus
      |      operationId: Tallenna uusi koulutus
      |      description: Tallenna uuden koulutuksen tiedot.
      |        Rajapinta palauttaa koulutukselle generoidun yksilöivän koulutus-oidin.
      |      tags:
      |        - Koulutus
      |      requestBody:
      |        description: Tallennettava koulutus
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Koulutus'
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                type: object
      |                properties:
      |                  oid:
      |                    type: string
      |                    description: Uuden koulutuksen yksilöivä oid
      |                    example: 1.2.246.562.13.00000000000000000009
      |""".stripMargin)
  put("/") {

    implicit val authenticated: Authenticated = authenticate

    koulutusService.create(parsedBody.extract[Koulutus]) map {
      case Right(oid) =>
        Ok("oid" -> oid)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }
}
