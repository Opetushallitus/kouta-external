package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.Julkaisutila
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.service.{HakukohdeSearchParams, HakukohdeService}
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.external.util.KoodistoUtil.markdownKoodistoLink
import fi.oph.kouta.service.RoleAuthorizationFailedException
import fi.oph.kouta.servlet.Authenticated
import fi.vm.sade.properties.OphProperties
import org.scalatra._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{ExecutionContext, Future}

object HakukohdeServlet extends HakukohdeServlet(HakukohdeService)

class HakukohdeServlet(hakukohdeService: HakukohdeService)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  def urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties

  override def executor: ExecutionContext = global

  private def parseOptionalBoolParam(params: Params, paramName: String): Option[Boolean] = params.get(paramName).map {
    case "true"  => true
    case "false" => false
  }

  private def parseOptionalMultiParam[T](param: Option[Seq[String]], f: (String) => T): Option[Set[T]] =
    Option(param.getOrElse(Seq()).filter(_.nonEmpty).map(f).toSet).filter(_.nonEmpty)
  private def parseOptionalMultiParam(param: Option[Seq[String]]): Option[Set[String]] =
    Option(param.getOrElse(Seq()).filter(_.nonEmpty).toSet).filter(_.nonEmpty)

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
        Ok(hakukohde, headers = createLastModifiedHeader(hakukohde))
      }
      .recoverWith { case _: RoleAuthorizationFailedException =>
        logger.info(s"Authorization failed hakukohde $hakukohdeOid, retrying with hakukohderyhmä rights.")
        hakukohdeService.getHakukohdeAuthorizeByHakukohderyhma(hakukohdeOid).map { hakukohde: Hakukohde =>
          Ok(hakukohde, headers = createLastModifiedHeader(hakukohde))
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
    s"""    get:
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
       |              enum:
       |                - julkaistu
       |                - tallennettu
       |                - arkistoitu
       |          required: false
       |          description: Suodata annetuilla tiloilla (julkaistu/tallennettu/arkistoitu)
       |        - in: query
       |          name: hakutapa
       |          schema:
       |            type: string
       |          required: false
       |          example: hakutapa_03
       |          description: Hakukohteen hakutapa. Viittaa koodistoon ${markdownKoodistoLink("hakutapa")}
       |        - in: query
       |          name: opetuskieli
       |          schema:
       |            type: array
       |            items:
       |              type: string
       |          required: false
       |          example: 
       |            - oppilaitoksenopetuskieli_1
       |          description: Hakukohteen opetuskielet. Viittaa koodistoon ${markdownKoodistoLink(
      "oppilaitoksenopetuskieli"
    )}
       |        - in: query
       |          name: alkamiskausi
       |          schema:
       |            type: string
       |          required: false
       |          example: kausi_s
       |          description: Koulutuksen alkamiskausi. Viittaa koodistoon ${markdownKoodistoLink("kausi")}
       |        - in: query
       |          name: alkamisvuosi
       |          schema:
       |            type: number
       |          required: false
       |          example: 2020
       |          description: Koulutuksen alkamisvuosi.
       |        - in: query
       |          name: koulutusaste
       |          schema:
       |            type: array
       |            items:
       |              type: string
       |          required: false
       |          example: 
       |            - kansallinenkoulutusluokitus2016koulutusastetaso1_5
       |          description: 'Koulutuksen koulutusaste. Viittaa koodistoihin ${markdownKoodistoLink(
      "kansallinenkoulutusluokitus2016koulutusastetaso1"
    )} ja ${markdownKoodistoLink("kansallinenkoulutusluokitus2016koulutusastetaso2")}'
       |      responses:
       |        '200':
       |          description: Ok
       |          content:
       |            application/json:
       |              schema:
       |                type: array
       |                items:
       |                  $$ref: '#/components/schemas/Hakukohde'
       |""".stripMargin
  )
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    val searchParams = HakukohdeSearchParams(
      hakuOid = params.get("haku").map(HakuOid),
      tarjoajaOids = parseOptionalMultiParam(multiParams.get("tarjoaja"), OrganisaatioOid),
      q = params.get("q"),
      all = parseOptionalBoolParam(params, "all").getOrElse(false),
      withHakukohderyhmat = parseOptionalBoolParam(params, "withHakukohderyhmat").getOrElse(false),
      johtaaTutkintoon = parseOptionalBoolParam(params, "johtaaTutkintoon"),
      tila = parseOptionalMultiParam(multiParams.get("tila"), Julkaisutila.withName),
      hakutapa = multiParams.get("hakutapa").map(_.toSet),
      opetuskieli = parseOptionalMultiParam(multiParams.get("opetuskieli")),
      alkamiskausi = params.get("alkamiskausi"),
      alkamisvuosi = params.get("alkamisvuosi"),
      koulutusaste = parseOptionalMultiParam(multiParams.get("koulutusaste"))
    )

    new AsyncResult() {
      override implicit def timeout: Duration = 5.minutes

      override val is: Future[ActionResult] = (searchParams.hakuOid, searchParams.tarjoajaOids) match {
        case (None, None)                   => Future.successful(BadRequest("You must give either haku or tarjoaja"))
        case (Some(oid), _) if !oid.isValid => Future.successful(BadRequest(s"Invalid haku ${oid.toString}"))
        case (_, Some(oids)) if oids.exists(!_.isValid) =>
          Future.successful(BadRequest(s"Invalid tarjoaja ${oids.find(!_.isValid).get.toString}"))
        case (_, _) =>
          hakukohdeService.search(searchParams).map(Ok(_)).recoverWith { case _: RoleAuthorizationFailedException =>
            logger.info(s"Authorization failed hakukohde search, retrying with hakukohderyhmä rights.")
            hakukohdeService
              .searchAuthorizeByHakukohderyhma(searchParams)
              .map(Ok(_))
          }
      }
    }
  }
}
