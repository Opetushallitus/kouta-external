package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.service.ToteutusService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, FutureSupport, Ok}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object ToteutusServlet extends ToteutusServlet(ToteutusService)

class ToteutusServlet(toteutusService: ToteutusService)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/toteutus/{oid}",
    """    get:
      |      summary: Hae koulutuksen toteutus
      |      operationId: Hae toteutus
      |      description: Hakee koulutuksen toteutuksen tiedot
      |      tags:
      |        - Toteutus
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: toteutus-oid
      |          example: 1.2.246.562.17.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Toteutus'
      |""".stripMargin)
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    toteutusService.get(ToteutusOid(params("oid")))
      .map { toteutus =>
        Ok(toteutus, headers = Map(KoutaServlet.LastModifiedHeader -> createLastModifiedHeader(toteutus)))
      }
  }

  registerPath( "/toteutus/",
    """    put:
      |      summary: Tallenna uusi toteutus
      |      operationId: Tallenna uusi toteutus
      |      description: Tallenna uuden toteutuksen tiedot.
      |        Rajapinta palauttaa toteutukselle generoidun yksilöivän toteutus-oidin.
      |      tags:
      |        - Toteutus
      |      requestBody:
      |        description: Tallennettava toteutus
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Toteutus'
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
      |                    description: Uuden toteutuksen yksilöivä oid
      |                    example: 1.2.246.562.17.00000000000000000009
      |""".stripMargin)
  put("/") {

    implicit val authenticated: Authenticated = authenticate

    toteutusService.create(parsedBody.extract[Toteutus]) map {
      case Right(oid) =>
        Ok("oid" -> oid)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }

  registerPath("/toteutus/",
    """    post:
      |      summary: Muokkaa olemassa olevaa toteutusta
      |      operationId: Muokkaa toteutusta
      |      description: Muokkaa olemassa olevaa toteutusta. Rajapinnalle annetaan toteutuksen kaikki tiedot,
      |        ja muuttuneet tiedot tallennetaan kantaan.
      |      tags:
      |        - Toteutus
      |      parameters:
      |        - $ref: '#/components/parameters/xIfUnmodifiedSince'
      |      requestBody:
      |        description: Muokattavan toteutuksen kaikki tiedot. Kantaan tallennetaan muuttuneet tiedot.
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Toteutus'
      |      responses:
      |        '200':
      |          description: Ok
      |""".stripMargin)
  post("/") {
    implicit val authenticated: Authenticated = authenticate

    toteutusService.update(parsedBody.extract[Toteutus], getIfUnmodifiedSince) map {
      case Right(response) =>
        Ok(response)
      case Left((status, message)) =>
        ActionResult(status, message, Map.empty)
    }
  }

}
