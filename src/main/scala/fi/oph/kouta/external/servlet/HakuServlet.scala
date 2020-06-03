package fi.oph.kouta.external.servlet

import java.util.UUID

import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.service.HakuService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, FutureSupport, NotFound, Ok}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object HakuServlet extends HakuServlet(HakuService)

class HakuServlet(hakuService: HakuService) extends KoutaServlet with CasAuthenticatedServlet with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/haku/{oid}",
    """    get:
      |      summary: Hae haun tiedot
      |      operationId: Hae haku
      |      description: Hakee haun kaikki tiedot
      |      tags:
      |        - Haku
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Haku-oid
      |          example: 1.2.246.562.29.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Haku'
      |""".stripMargin
  )
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    hakuService.get(HakuOid(params("oid")))
      .map { case (haku, modified) =>
        Ok(haku, headers = createLastModifiedHeader(modified))
      }
  }

  /*
  registerPath(
    "/haku/search",
    """    get:
      |      summary: Etsi hakuja
      |      operationId: Etsi hakuja
      |      description: Etsii hauista annetuilla ehdoilla
      |      tags:
      |        - Haku
      |      parameters:
      |        - in: query
      |          name: ataruId
      |          schema:
      |            type: string
      |          required: true
      |          description: Ataru-lomakkeen id
      |          example: 66b7b709-1ed0-49cc-bbef-e5b0420a81c9
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                type: array
      |                items:
      |                  $ref: '#/components/schemas/Haku'
      |""".stripMargin
  )
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    params.get("ataruId").map(UUID.fromString) match {
      case None => NotFound()
      case Some(id) =>
        hakuService.searchByAtaruId(id).map {
          case haut if haut.isEmpty =>
            NotFound(s"Didn't find anything searching for haku with $id in hakulomakeAtaruId")
          case haut => haut
        }
    }
  }
   */

  registerPath( "/haku/",
    """    post:
      |      summary: Tallenna uusi haku
      |      operationId: Tallenna uusi haku
      |      description: Tallenna uuden haun tiedot.
      |        Rajapinta palauttaa haulle generoidun yksilöivän haku-oidin.
      |      tags:
      |        - Haku
      |      requestBody:
      |        description: Tallennettava haku
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Haku'
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
      |                    description: Uuden haun yksilöivä oid
      |                    example: 1.2.246.562.29.00000000000000000009
      |""".stripMargin)
  post("/") {
    implicit val authenticated: Authenticated = authenticate

    hakuService.create(parsedBody.extract[Haku]) map {
      case Right(oid) =>
        Ok("oid" -> oid)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }

  registerPath("/haku/",
    """    put:
      |      summary: Muokkaa olemassa olevaa hakua
      |      operationId: Muokkaa hakua
      |      description: Muokkaa olemassa olevaa hakua. Rajapinnalle annetaan haun kaikki tiedot,
      |        ja muuttuneet tiedot tallennetaan kantaan.
      |      tags:
      |        - Haku
      |      parameters:
      |        - $ref: '#/components/parameters/xIfUnmodifiedSince'
      |      requestBody:
      |        description: Muokattavan haun kaikki tiedot. Kantaan tallennetaan muuttuneet tiedot.
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Haku'
      |      responses:
      |        '200':
      |          description: O
      |""".stripMargin)
  put("/") {
    implicit val authenticated: Authenticated = authenticate

    hakuService.update(parsedBody.extract[Haku], getIfUnmodifiedSince) map {
      case Right(response) =>
        Ok(response)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }
}
