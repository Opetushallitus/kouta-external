package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.oid.HakukohdeOid
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.HakukohdeService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class HakukohdeServlet
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath("/hakukohde/{oid}",
    """    get:
      |      summary: Hae hakukohteen tiedot
      |      operationId: Hae hakukohde
      |      description: Hakee hakukohteen kaikki tiedot
      |      tags:
      |        - Hakukohde
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Hakukohde-oid
      |          example: 1.2.246.562.20.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Hakukohde'
      |""".stripMargin)
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    HakukohdeService.get(HakukohdeOid(params("oid")))
  }

}
