package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.koutalight.KoutaLightKoulutus
import fi.oph.kouta.external.service.KoutaLightService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.security.Role
import fi.oph.kouta.security.Role.Paakayttaja
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{Forbidden, Ok}

import scala.util.{Failure, Success, Try}

object KoutaLightServlet extends KoutaLightServlet(KoutaLightService)

class KoutaLightServlet(koutaLightService: KoutaLightService) extends KoutaServlet with CasAuthenticatedServlet {
  private def errorMessage(message: String) = Map("errorMessage" -> message)

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
      |        '403':
      |          description: Forbidden
      |""".stripMargin
  )
  put("/") {
    if (externalModifyEnabled) {
      implicit val authenticated: Authenticated = authenticate

      // Käytetään koulutuksen omistavana organisaationa pääkäyttäjän organisaatiota kunnes saadaan oma käyttöoikeus
      Try(authenticated.session.getOrganizationsForRoles(Seq(Paakayttaja))) match {
        // Käyttäjällä saa olla vain yksi organisaatio liitettynä käyttöoikeuteen
        case Success(orgs) if orgs.size == 1 =>
          val ownerOrg         = orgs.head
          // Vain OphPääkäyttäjä voi tallentaa koulutuksia rajapinnan kautta kunnes saadaan oma käyttöoikeus
          val isOphPaakayttaja = authenticated.session.roles.contains(Role.Paakayttaja)

          if (isOphPaakayttaja) {
            Ok(koutaLightService.put(parsedBody.extract[List[KoutaLightKoulutus]], ownerOrg))
          } else {
            val errorMsg = errorMessage("Käyttäjällä ei ole oikeutta koulutusten tallentamiseen rajapinnan kautta")
            logger.warn(errorMsg.toString())
            Forbidden(errorMsg)
          }
        case Success(orgs) if orgs.size > 1 =>
          val errorMsg = errorMessage("Käyttäjän oikeuksissa määritelty liian monta organisaatiota")
          logger.warn(errorMsg.toString())
          Forbidden(errorMsg)
        case Success(orgs) if orgs.isEmpty =>
          val errorMsg = errorMessage("Käyttäjän oikeuksissa ei ole määritelty organisaatiota")
          logger.warn(errorMsg.toString())
          Forbidden(errorMsg)
        case Failure(e) =>
          logger.warn(e.toString)
          Forbidden(errorMessage("Käyttäjän oikeuksissa puutteita"))
      }
    } else {
      Forbidden(errorMessage("Rajapinnan käyttö estetty tässä ympäristössä"))
    }
  }
}
