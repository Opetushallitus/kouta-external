package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.koutalight.KoutaLightKoulutus
import fi.oph.kouta.external.service.KoutaLightService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.security.Role.Paakayttaja
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.ActionResult

object KoutaLightServlet extends KoutaLightServlet(KoutaLightService)

class KoutaLightServlet(koutaLightService: KoutaLightService) extends KoutaServlet with CasAuthenticatedServlet {

  registerPath(
    "/koutan-tietomallista-poikkeavat-koulutukset",
    """    put:
      |      summary: Tallenna koulutukset
      |      operationId: Tallenna koulutukset
      |      description: "Tallenna annettujen koulutusten tiedot. Päivittää aiemmin lisätyt koulutukset ja lisää puuttuvat.
      |        Päivitys tapahtuu koulutuksen externalId-kentän perusteella: jos tietokannasta löytyy koulutusta lisäävän organisaation koulutus samalla externalId:llä,
      |        koulutusta yritetään päivittää, muuten sellainen lisätään."
      |      tags:
      |        - KoutaLight
      |      requestBody:
      |        description: Tallennettavat koulutukset
      |        required: true
      |        content:
      |          application/json:
      |            schema:
      |              type: array
      |              items:
      |                $ref: '#/components/schemas/KoutaLightKoulutus'
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/KoutaLightMassResult'
      |        '401':
      |          description: Unauthorized
      |
      |""".stripMargin
  )
  put("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate
      // Käytetään pääkäyttäjän organisaatiota kunnes saadaan oma käyttöoikeus
      val authorizedOrg = authenticated.session.getOrganizationsForRoles(Seq(Paakayttaja)).head
      koutaLightService.put(parsedBody.extract[List[KoutaLightKoulutus]], authorizedOrg)
    } else {
      ActionResult(403, "Rajapinnan käyttö estetty tässä ympäristössä", Map.empty)
    }
  }
}
