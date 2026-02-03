package fi.oph.kouta.external.servlet

import fi.oph.kouta.domain.oid.HakukohdeOid
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.service.{HakukohdeService, MassService}
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, AsyncResult, FutureSupport}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{ExecutionContext, Future}

object MassHakukohdeServlet extends MassHakukohdeServlet(HakukohdeService)

class MassHakukohdeServlet(
    hakukohdeService: HakukohdeService
) extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/hakukohteet/",
    """    put:
      |      summary: Tallenna hakukohteet
      |      operationId: Tallenna hakukohteet
      |      description: Tallenna annettujen hakukohteiden tiedot.
      |        Päivittää aiemmin lisätyt hakukohteet ja lisää puuttuvat. Jos hakukohteella on oid-kenttä,
      |        sitä yritetään päivittää, muuten lisätä.
      |
      |        Rajapinta palauttaa lisäyksille tiedon sille annetusta oidista ja muutoksille tiedon,
      |        päivitettiinkö hakukohdetta. Vastaukset ovat samassa järjestyksessä kuin pyynnön hakukohteet.
      |
      |        Huom! Rajapinnassa ei ole samanaikaisten muokkausten suojaa, eli jos joku on muokannut tietoja esim.
      |        opintopolun käyttöliittymässä, nämä muutokset jyrätään.
      |      tags:
      |        - Hakukohde
      |      requestBody:
      |        description: Tallennettava hakukohde
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              type: array
      |              items:
      |                $ref: '#/components/schemas/Hakukohde'
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/MassResult'
      |""".stripMargin
  )
  put("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate
      new AsyncResult {
        override def timeout: Duration = 15.minutes
        override val is: Future[_]     = hakukohdeService.massImport(parsedBody.extract[List[Hakukohde]])
      }
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
    }
  }
}
