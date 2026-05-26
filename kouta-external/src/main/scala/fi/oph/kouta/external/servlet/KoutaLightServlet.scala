package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.ExternalKoutaLightKoulutus
import fi.oph.kouta.external.service.KoutaLightService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.security.Role.KoutaLightTallentajaRole
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{Forbidden, Ok}

object KoutaLightServlet extends KoutaLightServlet(KoutaLightService)

class KoutaLightServlet(koutaLightService: KoutaLightService) extends KoutaServlet with CasAuthenticatedServlet {
  private def errorMessage(message: String) = Map("error" -> message)

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
    implicit val authenticated: Authenticated = authenticate
    val userOid                               = authenticated.session.personOid
    val userAuthoritiesStr                    = s"Käyttäjän käyttöoikeudet: ${authenticated.session.authorities}"
    def logWarningMessage(errorMessage: String): Unit =
      logger.warn(s"$errorMessage $userAuthoritiesStr")
    val hasKoutaLightRole = authenticated.session.roles.contains(KoutaLightTallentajaRole)

    if (hasKoutaLightRole) {
      val orgsForTheRole = authenticated.session.getOrganizationsForRoles(Seq(KoutaLightTallentajaRole))
      // Käyttäjällä saa olla vain yksi organisaatio liitettynä käyttöoikeuteen, koska sitä käytetään
      // koulutuksen omistajana tietokantaan tallennettaessa
      if (orgsForTheRole.size == 1) {
        val ownerOrg = orgsForTheRole.head
        Ok(koutaLightService.put(parsedBody.extract[List[ExternalKoutaLightKoulutus]], ownerOrg))
      } else if (orgsForTheRole.size > 1) {
        val errorStr = s"Käyttäjän $userOid oikeuksissa määritelty liian monta organisaatiota."
        logWarningMessage(errorStr)
        Forbidden(errorMessage(errorStr))
      } else {
        val errorStr = s"Käyttäjän $userOid oikeuksissa puutteita."
        logWarningMessage(errorStr)
        Forbidden(errorMessage(errorStr))
      }
    } else {
      val errorStr = s"Käyttäjällä $userOid ei ole oikeutta koulutusten tallentamiseen rajapinnan kautta."
      logWarningMessage(errorStr)
      Forbidden(errorMessage(errorStr))
    }
  }
}
