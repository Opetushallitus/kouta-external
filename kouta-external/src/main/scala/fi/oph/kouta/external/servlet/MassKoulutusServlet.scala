package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.service.KoulutusService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.external.{KoutaConfiguration, KoutaConfigurationFactory}
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, AsyncResult, FutureSupport}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object MassOperations {
  private val config: KoutaConfiguration = KoutaConfigurationFactory.configuration
  private val numThreads = config.massOperationConfiguration.numThreds

  val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))
}

object MassKoulutusServlet extends MassKoulutusServlet(KoulutusService)

class MassKoulutusServlet(koulutusService: KoulutusService)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/koulutukset/",
    """    put:
      |      summary: Tallenna koulutukset
      |      operationId: Tallenna koulutukset
      |      description: Tallenna annettujen koulutusten tiedot.
      |        Päivittää aiemmin lisätyt koulutukset ja lisää puuttuvat. Jos koulutuksella on oid-kenttä,
      |        sitä yritetään päivittää, muuten lisätä.
      |
      |        Rajapinta palauttaa lisäyksille tiedon sille annetusta oidista ja muutoksille tiedon,
      |        päivitettiinkö koulutusta. Vastaukset ovat samassa järjestyksessä kuin pyynnön koulutukset.
      |
      |        Huom! Rajapinnassa ei ole samanaikaisten muokkausten suojaa, eli jos joku on muokannut tietoja esim.
      |        opintopolun käyttöliittymässä, nämä muutokset jyrätään.
      |      tags:
      |        - Koulutus
      |      requestBody:
      |        description: Tallennettava koulutus
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              type: array
      |              items:
      |                $ref: '#/components/schemas/Koulutus'
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
        override val is: Future[_] = koulutusService.massImport(parsedBody.extract[List[Koulutus]])
      }
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
    }
  }
}
