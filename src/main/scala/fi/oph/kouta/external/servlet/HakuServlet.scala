package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.service.HakuService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, AsyncResult, BadRequest, FutureSupport, Ok}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
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

    hakuService.get(HakuOid(params("oid"))).map { case (haku, modified) =>
      Ok(haku, headers = createLastModifiedHeader(modified))
    }
  }

  registerPath(
    "/haku/",
    """    put:
      |      summary: Tallenna uusi haku
      |      operationId: Tallenna uusi haku
      |      description: Tallenna uuden haun tiedot.
      |        Rajapinta palauttaa haulle generoidun yksilöivän haku-oidin
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
      |""".stripMargin
  )
  put("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate

      hakuService.create(parsedBody.extract[Haku]) map {
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
    "/haku/",
    """    post:
      |      summary: Muokkaa olemassa olevaa hakua
      |      operationId: Muokkaa hakua
      |      description: Muokkaa olemassa olevaa hakua. Rajapinnalle annetaan haun kaikki tiedot,
      |        ja muuttuneet tiedot tallennetaan kantaan
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
      |""".stripMargin
  )
  post("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate

      hakuService.update(parsedBody.extract[Haku], getIfUnmodifiedSince) map {
        case Right(response) =>
          Ok(response)
        case Left((status, message)) =>
          ActionResult(status, message, Map.empty)
      }
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
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
      |          content:
      |             application/json:
      |               schema:
      |                 type: array
      |                 items:
      |                   type: string
      |               example: ["1.2.246.562.29.00000000000000000009","1.2.246.562.29.00000000000000000186"]
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
      |          required: false
      |          description: Ataru-lomakkeen id
      |          example: 66b7b709-1ed0-49cc-bbef-e5b0420a81c9
      |        - in: query
      |          name: tarjoaja
      |          schema:
      |            type: array
      |            items:
      |              type: string
      |          required: false
      |          description: Organisaatio joka on haun hakukohteen tarjoaja
      |          example: 1.2.246.562.10.00000000001,1.2.246.562.10.00000000002
      |        - in: query
      |          name: includeHakukohdeOids
      |          schema:
      |            type: boolean
      |          required: false
      |          description: hakukohteen oidit paluuarvoon
      |          example: true
      |        - in: query
      |          name: vuosi
      |          schema:
      |            type: integer
      |          required: false
      |          description: viimeiseksi alkaneen hakuajan vuosi tai koulutuksen alkamisvuosi
      |          example: 2022
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

    val ataruId  = params.get("ataruId")
    val tarjoaja = params.get("tarjoaja").map(_.split(",").map(OrganisaatioOid).toSet)
    val vuosi    = params.getAs[Int]("vuosi")
    val includeHakukohdeOids = params.get("includeHakukohdeOids").exists {
      case "true"  => true
      case "false" => false
    }

    logger.debug(s"Request: /haku/search | ataruId: ${ataruId} | tarjoaja: ${tarjoaja}")

    new AsyncResult() {
      override implicit def timeout: Duration = 5.minutes

      override val is: Future[ActionResult] = tarjoaja match {
        case tarjoaja => hakuService.search(ataruId, tarjoaja, vuosi, includeHakukohdeOids).map(Ok(_))
      }
    }

  }
}
