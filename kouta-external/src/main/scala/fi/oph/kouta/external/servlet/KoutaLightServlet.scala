package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.koutalight.KoutaLightKoulutus
import fi.oph.kouta.external.service.KoutaLightService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.security.Role
import fi.oph.kouta.security.Role.Paakayttaja
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{Forbidden, Ok}

object KoutaLightTallentajaRole extends Role.UnknownRole("APP_KOUTA_EXTERNAL_KOUTA_LIGHT_TALLENTAJA")

object KoutaLightServlet extends KoutaLightServlet(KoutaLightService)

class KoutaLightServlet(koutaLightService: KoutaLightService) extends KoutaServlet with CasAuthenticatedServlet {
  private def errorMessage(message: String) = Map("errorMessage" -> message)

  registerPath(
    "/koutan-tietomallista-poikkeavat-koulutukset/",
    """    put:
      |      summary: Tallenna koutan tietomallista poikkeavat koulutukset
      |      operationId: Tallenna koutan tietomallista poikkeavat koulutukset
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
      |        '403':
      |          description: Forbidden
      |""".stripMargin
  )
  put("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate

      // Vain OphPääkäyttäjä voi tallentaa koulutuksia rajapinnan kautta kunnes saadaan oma käyttöoikeus
      val isOphPaakayttaja = authenticated.session.roles.contains(Role.Paakayttaja)
      val hasKoutaLightRole = authenticated.session.roles.contains(KoutaLightTallentajaRole)

      if (hasKoutaLightRole) {
        val orgsForTheRole = authenticated.session.getOrganizationsForRoles(Seq(KoutaLightTallentajaRole))
        // Käyttäjällä saa olla vain yksi organisaatio liitettynä käyttöoikeuteen, koska sitä käytetään
        // koulutuksen omistajana tietokantaan talleennettaessa
        if (orgsForTheRole.size == 1) {
          val ownerOrg = orgsForTheRole.head
          Ok(koutaLightService.put(parsedBody.extract[List[KoutaLightKoulutus]], ownerOrg))
        } else if (orgsForTheRole.size > 1) {
          val errorMsg = errorMessage("Käyttäjän oikeuksissa määritelty liian monta organisaatiota")
          logger.warn(errorMsg.toString())
          Forbidden(errorMsg)
        } else {
          Forbidden(errorMessage("Käyttäjän oikeuksissa puutteita"))
        }
      } else {
        val errorMsg = errorMessage("Käyttäjällä ei ole oikeutta koulutusten tallentamiseen rajapinnan kautta")
        logger.warn(errorMsg.toString())
        Forbidden(errorMsg)
      }
    } else {
      Forbidden(errorMessage("Rajapinnan käyttö estetty tässä ympäristössä"))
    }
  }
}
