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

    koulutusService.get(KoulutusOid(params("oid"))).map { koulutus =>
      Ok(koulutus, headers = createLastModifiedHeader(koulutus))
    }
  }

  registerPath(
    "/koulutus/",
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
      |""".stripMargin
  )
  put("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate
      koulutusService.create(parsedBody.extract[Koulutus]) map {
        case Right(oid) =>
          Ok("oid" -> oid)
        case Left((status, message)) =>
          ActionResult(status, message, Map.empty)
      }
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
    }
  }

  registerPath(
    "/koulutus/",
    """    post:
      |      summary: Muokkaa olemassa olevaa koulutusta
      |      operationId: Muokkaa koulutusta
      |      description: Muokkaa olemassa olevaa koulutusta. Rajapinnalle annetaan koulutuksen kaikki tiedot,
      |        ja muuttuneet tiedot tallennetaan kantaan.
      |      tags:
      |        - Koulutus
      |      parameters:
      |        - $ref: '#/components/parameters/xIfUnmodifiedSince'
      |      requestBody:
      |        description: Muokattavan koulutuksen kaikki tiedot. Kantaan tallennetaan muuttuneet tiedot.
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Koulutus'
      |      responses:
      |        '200':
      |          description: Ok
      |""".stripMargin
  )
  post("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate

      koulutusService.update(parsedBody.extract[Koulutus], getIfUnmodifiedSince) map {
        case Right(response) =>
          Ok(response)
        case Left((status, message)) =>
          ActionResult(status, message, Map.empty)
      }
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
    }
  }
}
