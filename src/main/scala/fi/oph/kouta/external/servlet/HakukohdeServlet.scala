package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.oid.HakukohdeOid
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.service.HakukohdeService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{FutureSupport, Ok}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object HakukohdeServlet extends HakukohdeServlet(HakukohdeService)

class HakukohdeServlet(hakukohdeService: HakukohdeService)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/hakukohde/{oid}",
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
      |""".stripMargin
  )
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    hakukohdeService.get(HakukohdeOid(params("oid")))
      .map { hakukohde: Hakukohde =>
        Ok(hakukohde, headers = Map(KoutaServlet.LastModifiedHeader -> createLastModifiedHeader(hakukohde)))
      }
  }

}
