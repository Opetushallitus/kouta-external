package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Valintaperuste

import java.util.UUID
import fi.oph.kouta.external.service.ValintaperusteService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, FutureSupport, Ok}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object ValintaperusteServlet extends ValintaperusteServlet(ValintaperusteService)

class ValintaperusteServlet(valintaperusteService: ValintaperusteService)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/valintaperuste/{id}",
    """    get:
      |      summary: Hae valintaperustekuvauksen tiedot
      |      operationId: Hae valintaperuste
      |      description: Hakee valintaperustekuvauksen kaikki tiedot
      |      tags:
      |        - Valintaperuste
      |      parameters:
      |        - in: path
      |          name: id
      |          schema:
      |            type: string
      |          required: true
      |          description: Valintaperuste-id
      |          example: ea596a9c-5940-497e-b5b7-aded3a2352a7
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Valintaperuste'
      |""".stripMargin
  )
  get("/:id") {
    implicit val authenticated: Authenticated = authenticate

    valintaperusteService.get(UUID.fromString(params("id")))
      .map { valintaperuste =>
        Ok(valintaperuste, headers = Map(KoutaServlet.LastModifiedHeader -> createLastModifiedHeader(valintaperuste)))
      }
  }

  registerPath( "/valintaperuste/",
    """    put:
      |      summary: Tallenna uusi valintaperustekuvaus
      |      operationId: Tallenna uusi valintaperuste
      |      description: Tallenna uuden valintaperustekuvauksen tiedot.
      |        Rajapinta palauttaa valintaperustekuvaukselle generoidun yksilöivän id:n
      |      tags:
      |        - Valintaperuste
      |      requestBody:
      |        description: Tallennettava valintaperustekuvaus
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Valintaperuste'
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                type: object
      |                properties:
      |                  id:
      |                    type: string
      |                    description: Uuden valintaperustekuvauksen yksilöivä id
      |                    example: ea596a9c-5940-497e-b5b7-aded3a2352a7
      |""".stripMargin)
  put("/") {
    implicit val authenticated: Authenticated = authenticate

    valintaperusteService.create(parsedBody.extract[Valintaperuste]) map {
      case Right(id) =>
        Ok("id" -> id)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }

  registerPath("/valintaperuste/",
    """    post:
      |      summary: Muokkaa olemassa olevaa valintaperustekuvausta
      |      operationId: Muokkaa valintaperustetta
      |      description: Muokkaa olemassa olevaa valintaperustekuvausta. Rajapinnalle annetaan valintaperusteen kaikki tiedot,
      |        ja muuttuneet tiedot tallennetaan kantaan.
      |      tags:
      |        - Valintaperuste
      |      requestBody:
      |        description: Muokattavan valintaperustekuvauksen kaikki tiedot. Kantaan tallennetaan muuttuneet tiedot.
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Valintaperuste'
      |      responses:
      |        '200':
      |          description: O
      |""".stripMargin)
  post("/") {
    implicit val authenticated: Authenticated = authenticate

    valintaperusteService.update(parsedBody.extract[Valintaperuste], getIfUnmodifiedSince) map {
      case Right(response) =>
        Ok(response)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }

}
