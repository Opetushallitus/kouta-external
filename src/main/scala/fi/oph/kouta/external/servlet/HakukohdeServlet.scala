package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.Julkaisutila
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.indexed.KoodiUri
import fi.oph.kouta.external.service.HakukohdeService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.service.RoleAuthorizationFailedException
import fi.oph.kouta.servlet.Authenticated
import org.scalatra._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{ExecutionContext, Future}

object HakukohdeServlet extends HakukohdeServlet(HakukohdeService)

case class HakukohdeSearchParams(
    haku: Option[HakuOid] = None,
    tarjoaja: Option[Set[OrganisaatioOid]] = None,
    q: Option[String] = None,
    all: Boolean = false,
    withHakukohderyhmat: Boolean = false,
    johtaaTutkintoon: Option[Boolean] = None,
    tila: Option[Set[Julkaisutila]] = None,
    hakutapa:  Option[KoodiUri] = None,
    opetuskielet: Option[Set[KoodiUri]] = None,
    alkamiskausi: Option[KoodiUri] = None,
    alkamisvuosi: Option[Number] = None
)

class HakukohdeServlet(hakukohdeService: HakukohdeService)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  private def parseOptionalBoolParam(params: Params, paramName: String): Option[Boolean] = params.get(paramName).map {
    case "true" => true
    case "false" => false
  }

  registerPath(
    "/hakukohde/{oid}",
    """    get:
      |      summary: Hae hakukohteen tiedot
      |      operationId: getHakukohde
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
    val hakukohdeOid                          = HakukohdeOid(params("oid"))
    hakukohdeService
      .get(hakukohdeOid)
      .map { hakukohde: Hakukohde =>
        Ok(hakukohde, headers = Map(KoutaServlet.LastModifiedHeader -> createLastModifiedHeader(hakukohde)))
      }
      .recoverWith { case _: RoleAuthorizationFailedException =>
        logger.info(s"Authorization failed hakukohde $hakukohdeOid, retrying with hakukohderyhmä rights.")
        hakukohdeService.getHakukohdeAuthorizeByHakukohderyhma(hakukohdeOid).map { hakukohde: Hakukohde =>
          Ok(hakukohde, headers = Map(KoutaServlet.LastModifiedHeader -> createLastModifiedHeader(hakukohde)))
        }
      }
  }

  registerPath(
    "/hakukohde/",
    """    put:
      |      summary: Tallenna uusi hakukohde
      |      operationId: createHakukohde
      |      description: Tallenna uuden hakukohteen tiedot.
      |        Rajapinta palauttaa hakukohteelle generoidun yksilöivän hakukohde-oidin
      |      tags:
      |        - Hakukohde
      |      requestBody:
      |        description: Tallennettava hakukohde
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Hakukohde'
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
      |                    description: Uuden hakukohteen yksilöivä oid
      |                    example: 1.2.246.562.20.00000000000000000009
      |""".stripMargin
  )
  put("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate

      hakukohdeService.create(parsedBody.extract[Hakukohde]) map {
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
    "/hakukohde/",
    """    post:
      |      summary: Muokkaa olemassa olevaa hakukohdetta
      |      operationId: editHakukohde
      |      description: Muokkaa olemassa olevaa hakukohdetta. Rajapinnalle annetaan hakukohteen kaikki tiedot,
      |        ja muuttuneet tiedot tallennetaan kantaan.
      |      tags:
      |        - Hakukohde
      |      parameters:
      |        - $ref: '#/components/parameters/xIfUnmodifiedSince'
      |      requestBody:
      |        description: Muokattavan hakukohteen kaikki tiedot. Kantaan tallennetaan muuttuneet tiedot.
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              $ref: '#/components/schemas/Hakukohde'
      |      responses:
      |        '200':
      |          description: O
      |""".stripMargin
  )
  post("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate

      hakukohdeService.update(parsedBody.extract[Hakukohde], getIfUnmodifiedSince) map {
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
    "/hakukohde/search",
    """    get:
      |      summary: Etsi hakukohteita
      |      operationId: searchHakukohteet
      |      description: Etsii hakukohteista annetuilla ehdoilla
      |      tags:
      |        - Hakukohde
      |      parameters:
      |        - in: query
      |          name: johtaaTutkintoon
      |          schema:
      |            type: boolean
      |          required: false
      |          description: Onko hakukohde liitetty tutkintoon johtavaan koulutukseen?
      |          example: false
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
      |        - in: query
      |          name: withHakukohderyhmat
      |          schema:
      |            type: boolean
      |            default: false
      |          required: false
      |          description: Haetaanko hakukohderyhmien tunnisteet hakukohteelle
      |          example: false
      |        - in: query
      |          name: tila
      |          schema:
      |            type: array
      |            items:
      |              type: string
      |          required: false
      |          description: Suodata annetuilla tiloilla (julkaistu/tallennettu/arkistoitu)
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
    val tarjoaja = multiParams.get("tarjoaja").map(_.map(OrganisaatioOid).toSet)
    val q        = params.get("q")

    val all = parseOptionalBoolParam(params, "all").getOrElse(false)
    val withHakukohderyhmat = parseOptionalBoolParam(params, "withHakukohderyhmat").getOrElse(false)
    val johtaaTutkintoon = parseOptionalBoolParam(params, "johtaaTutkintoon")

    val tila = multiParams.get("tila").map(_.map(Julkaisutila.withName).toSet)

    val searchParams = HakukohdeSearchParams(
      tila = tila,
      johtaaTutkintoon = johtaaTutkintoon,
      withHakukohderyhmat = withHakukohderyhmat,
      all = all,
      tarjoaja = tarjoaja,
      q = q,
    )

    new AsyncResult() {
      override implicit def timeout: Duration = 5.minutes

      override val is: Future[ActionResult] = (hakuOid, tarjoaja) match {
        case (None, None)                   => Future.successful(BadRequest("Query parameter is required"))
        case (Some(oid), _) if !oid.isValid => Future.successful(BadRequest(s"Invalid haku ${oid.toString}"))
        case (_, Some(oids)) if oids.exists(!_.isValid) =>
          Future.successful(BadRequest(s"Invalid tarjoaja ${oids.find(!_.isValid).get.toString}"))
        case (hakuOid, tarjoajaOids) =>
          hakukohdeService.search(hakuOid, tarjoajaOids, q, all, withHakukohderyhmat).map(Ok(_)).recoverWith {
            case _: RoleAuthorizationFailedException =>
              logger.info(s"Authorization failed hakukohde search, retrying with hakukohderyhmä rights.")
              hakukohdeService
                .searchAuthorizeByHakukohderyhma(hakuOid, tarjoajaOids, q, all, withHakukohderyhmat)
                .map(Ok(_))
          }
      }
    }
  }
}
