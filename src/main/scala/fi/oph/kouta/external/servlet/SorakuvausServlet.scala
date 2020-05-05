package fi.oph.kouta.external.servlet

import java.util.UUID

import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.SorakuvausService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import org.scalatra.{FutureSupport, Ok}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object SorakuvausServlet extends SorakuvausServlet(SorakuvausService)

class SorakuvausServlet(sorakuvausService: SorakuvausService)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/sorakuvaus/{id}",
    """    get:
      |      summary: Hae sorakuvaus
      |      description: Hakee sorakuvauksen tiedot
      |      operationId: Hae sorakuvaus
      |      tags:
      |        - Sorakuvaus
      |      parameters:
      |        - in: path
      |          name: id
      |          schema:
      |            type: string
      |          required: true
      |          description: Sorakuvaus-id
      |          example: ea596a9c-5940-497e-b5b7-aded3a2352a7
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Sorakuvaus'
      |""".stripMargin
  )
  get("/:id") {
    implicit val authenticated: Authenticated = authenticate

    sorakuvausService.get(UUID.fromString(params("id")))
      .map { sorakuvaus =>
        Ok(sorakuvaus, headers = Map(KoutaServlet.LastModifiedHeader -> createLastModifiedHeader(sorakuvaus)))
      }
  }

}
