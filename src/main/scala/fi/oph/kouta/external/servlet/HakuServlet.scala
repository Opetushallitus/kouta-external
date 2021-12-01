package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.service.HakuService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, BadRequest, FutureSupport, Ok}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

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

  registerPath(
    "/haku/findbyoids",
    """    post:
      |      summary: Etsi hakuja oideilla
      |      operationId: Etsi hakuja oideilla
      |      description: Etsii hakuja annetuilla oideilla
      |      tags:
      |        - Haku
      |      requestBody:
      |          description: Palautettavien hakujen oidit JSON-arrayna
      |          example: ["1.2.246.562.29.00000000000000000009","1.2.246.562.29.00000000000000000186"]
      |          content:
      |             application/json:
      |               schema:
      |                 type: array
      |                 items:
      |                   type: string
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
  post("/findbyoids") {
    implicit val authenticated: Authenticated = authenticate

    Try(parsedBody.extract[Set[HakuOid]]).toEither match {
      case Left(e) =>
        logger.warn("Failed to parse hakuOids", e)
        BadRequest(s"Failed to parse hakuOids: ${e.getMessage}")
      case Right(oids) if oids.isEmpty => Set()
      case Right(oids) if oids.exists(!_.isValid) =>
        BadRequest(s"Invalid hakuOids ${oids.find(!_.isValid).get.toString}")
      case Right(hakuOids) => hakuService.findByOids(hakuOids)
    }
  }

}
