package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.{Hakukohde, Sorakuvaus}

import java.util.UUID
import fi.oph.kouta.external.service.SorakuvausService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, FutureSupport, Ok}

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

  registerPath( "/sorakuvaus/",
    """    put:
      |      summary: Tallenna uusi sorakuvaus
      |      operationId: Tallenna uusi sorakuvaus
      |      description: Tallenna uuden sorakuvauksen tiedot.
      |        Rajapinta palauttaa sorakuvaukselle generoidun yksilöivän id:n
      |      tags:
      |        - Sorakuvaus
      |      requestBody:
      |        description: Tallennettava sorakuvaus
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Sorakuvaus'
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
      |                    description: Uuden SORA-kuvauksen yksilöivä id
      |                    example: ea596a9c-5940-497e-b5b7-aded3a2352a7
      |""".stripMargin)
  put("/") {
    implicit val authenticated: Authenticated = authenticate

    sorakuvausService.create(parsedBody.extract[Sorakuvaus]) map {
      case Right(id) =>
        Ok("id" -> id)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }

  registerPath("/sorakuvaus/",
    """    post:
      |      summary: Muokkaa olemassa olevaa SORA-kuvausta
      |      operationId: Muokkaa sorakuvausta
      |      description: Muokkaa olemassa olevaa SORA-kuvausta. Rajapinnalle annetaan SORA-kuvauksen kaikki tiedot,
      |        ja muuttuneet tiedot tallennetaan kantaan.
      |      tags:
      |        - Sorakuvaus
      |      parameters:
      |        - $ref: '#/components/parameters/xIfUnmodifiedSince'
      |      requestBody:
      |        description: Muokattavan SORA-kuvauksen kaikki tiedot. Kantaan tallennetaan muuttuneet tiedot.
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Sorakuvaus'
      |      responses:
      |        '200':
      |          description: O
      |""".stripMargin)
  post("/") {
    implicit val authenticated: Authenticated = authenticate

    sorakuvausService.update(parsedBody.extract[Sorakuvaus], getIfUnmodifiedSince) map {
      case Right(response) =>
        Ok(response)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }

}
