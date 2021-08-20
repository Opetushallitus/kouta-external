package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.service.HakukohdeService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{BadRequest, FutureSupport, Ok}

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

  registerPath(
    "/hakukohde/search",
    """    get:
      |      summary: Etsi hakukohteita
      |      operationId: Etsi hakukohteita
      |      description: Etsii hakukohteista annetuilla ehdoilla
      |      tags:
      |        - Hakukohde
      |      parameters:
      |        - in: query
      |          name: haku
      |          schema:
      |            type: string
      |          required: false
      |          description: Haun-oid
      |          example: 1.2.246.562.29.00000000000000000009
      |        - in: query
      |          name: tarjoaja
      |          schema:
      |            type: array
      |            items:
      |              type: string
      |          required: false
      |          description: Organisaatio joka on hakukohteen tarjoaja
      |          example: 1.2.246.562.10.00000000001,1.2.246.562.10.00000000002
      |        - in: query
      |          name: q
      |          schema:
      |            type: string
      |          required: false
      |          description: Tekstihaku hakukohteen ja sen järjestyspaikan nimeen
      |          example: Autoalan perustutkinto
      |        - in: query
      |          name: all
      |          schema:
      |            type: boolean
      |          required: false
      |          description: Haetaanko myös muiden, kuin annettujen tarjoajien hakukohteet
      |          example: true
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                type: array
      |                items:
      |                  $ref: '#/components/schemas/Hakukohde'
      |""".stripMargin
  )
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    val hakuOid  = params.get("haku").map(HakuOid)
    val tarjoaja = params.get("tarjoaja").map(s => s.split(",").map(OrganisaatioOid).toSet)
    val q        = params.get("q")
    val all = params.get("all").exists {
      case "true"  => true
      case "false" => false
    }

    (hakuOid, tarjoaja) match {
      case (None, None)                     => BadRequest("Query parameter is required")
      case (Some(oid), _) if !oid.isValid => BadRequest(s"Invalid haku ${oid.toString}")
      case (_, Some(oids)) if oids.exists(!_.isValid) =>
        BadRequest(s"Invalid tarjoaja ${oids.find(!_.isValid).get.toString}")
      case (hakuOid, tarjoajaOids) => hakukohdeService.search(hakuOid, tarjoajaOids, q, all)
    }
  }
}
