package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.service.KoulutusService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.external.{KoutaConfiguration, KoutaConfigurationFactory}
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{ActionResult, FutureSupport}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

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
      |                type: array
      |                items:
      |                  oneOf:
      |                    - type: object
      |                      description: Onnistunut luonti
      |                      properties:
      |                        operation:
      |                          const: CREATE
      |                        success:
      |                          type: boolean
      |                          const: true
      |                          description: Onnistuiko pyynnön käsittely
      |                        oid:
      |                          type: string
      |                          description: Uuden koulutuksen yksilöivä oid
      |                          example: 1.2.246.562.13.00000000000000000009
      |                    - type: object
      |                      description: Onnistunut päivitys
      |                      properties:
      |                        operation:
      |                          const: UPDATE
      |                        success:
      |                          type: boolean
      |                          const: true
      |                          description: Onnistuiko pyynnön käsittely
      |                        updated:
      |                          type: boolean
      |                          description: Oliko koulutuksessa päivitettävää
      |                          example: true
      |                    - type: object
      |                      description: Virhe koulutusta talletettaessa. Koulutusta ei ole lisätty / päivitetty.
      |                      properties:
      |                        operation:
      |                          enum: [CREATE, UPDATE]
      |                        success:
      |                          type: boolean
      |                          const: false
      |                          description: Onnistuiko pyynnön käsittely
      |                        status:
      |                          type: int
      |                          description: HTTP-vastauskoodi loppupalvelimelta
      |                          example: 403
      |                        message:
      |                          type: string
      |                          description: Vastauksen sisältö loppupalvelimelta
      |                    - type: object
      |                      description: Odottamaton virhe. Koulutus voi olla talletettu tai sitten ei.
      |                      properties:
      |                        operation:
      |                          enum: [CREATE, UPDATE]
      |                        success:
      |                          type: boolean
      |                          const: false
      |                          description: Onnistuiko pyynnön käsittely
      |                        exception:
      |                          type: string
      |                          description: Tapahtuneen virheen tyyppi
      |                          example: java.lang.IllegalArgumentException
      |""".stripMargin
  )
  put("/") {
    logger.error("PUT /koulutukset")
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate
      koulutusService.massImport(parsedBody.extract[List[Koulutus]])
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
    }
  }
}
