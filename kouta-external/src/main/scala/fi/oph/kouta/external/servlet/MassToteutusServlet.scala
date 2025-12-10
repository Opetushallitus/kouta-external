package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.service.ToteutusService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, AsyncResult, FutureSupport}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{ExecutionContext, Future}

object MassToteutusServlet extends MassToteutusServlet(ToteutusService)

class MassToteutusServlet(
    toteutusService: ToteutusService
) extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/toteutukset/",
    """    put:
      |      summary: Tallenna toteutukset
      |      operationId: Tallenna toteutukset
      |      description: Tallenna annettujen toteutusten tiedot.
      |        Päivittää aiemmin lisätyt toteutukset ja lisää puuttuvat. Jos toteutuksella on oid-kenttä,
      |        sitä yritetään päivittää, muuten lisätä.
      |
      |        Rajapinta palauttaa lisäyksille tiedon sille annetusta oidista ja muutoksille tiedon,
      |        päivitettiinkö toteutusta. Vastaukset ovat samassa järjestyksessä kuin pyynnön toteutukset.
      |
      |        Huom! Rajapinnassa ei ole samanaikaisten muokkausten suojaa, eli jos joku on muokannut tietoja esim.
      |        opintopolun käyttöliittymässä, nämä muutokset jyrätään.
      |      tags:
      |        - Toteutus
      |      requestBody:
      |        description: Tallennettava toteutus
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              type: array
      |              items:
      |                $ref: '#/components/schemas/Toteutus'
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
    logger.error("PUT /toteutukset")
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate
      new AsyncResult {
        override def timeout: Duration = 15.minutes
        override val is: Future[_]     = toteutusService.massImport(parsedBody.extract[List[Toteutus]])
      }
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
    }
  }
}
