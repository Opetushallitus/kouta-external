package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.domain.siirtotiedosto.SiirtotiedostoDateTimeFormatter
import fi.oph.kouta.external.service.{KoutaLightSiirtotiedostoService, SiirtotiedostoOperationResults}
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.security.Role
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.{Forbidden, Ok}
import org.json4s.jackson.Serialization.writePretty

import java.time.Instant
import java.util.UUID
import scala.util.{Failure, Try}

class KoutaLightSiirtotiedostoServlet(koutaLightSiirtotiedostoService: KoutaLightSiirtotiedostoService)
    extends KoutaServlet
    with CasAuthenticatedServlet {

  private def parseTimeRange(
      startTime: Option[String],
      endTime: Option[String] = None
  ): (Option[Instant], Instant) = {
    val defaultEndTime = Instant.now()
    val startTimeVal   = parseInstant(startTime, "alkuaika")
    val endTimeVal     = parseInstant(endTime, "loppuaika").getOrElse(defaultEndTime)
    startTimeVal match {
      case Some(startTimeVal) if startTimeVal.isAfter(Instant.now()) =>
        throw new IllegalArgumentException("Alkuaika ei voi olla tulevaisuudessa")
      case Some(startTimeVal) if startTimeVal.isAfter(endTimeVal) =>
        throw new IllegalArgumentException("Alkuaika ei voi olla loppuajan jälkeen")
      case _ => (startTimeVal, endTimeVal)
    }
  }

  private def parseInstant(
      dateTime: Option[String],
      fieldName: String
  ): Option[Instant] = {
    dateTime.map { dateTimeStr =>
      Try(Instant.from(SiirtotiedostoDateTimeFormatter.parse(dateTimeStr))).recoverWith { case _ =>
        Failure(new IllegalArgumentException(s"Virheellinen $fieldName '$dateTimeStr'"))
      }.get
    }
  }

  private def resultMap(operationResults: SiirtotiedostoOperationResults) =
    Map(
      "keys"    -> operationResults.s3ObjectKeys.mkString(", "),
      "count"   -> operationResults.count.toString,
      "success" -> "true"
    )

  private val DateTimeExample = SiirtotiedostoDateTimeFormatter.format(Instant.now())

  registerPath(
    "/siirtotiedosto/kouta-light-koulutukset",
    s"""    get:
       |      summary: Tallenna koutan tietomallista poikkeavat koulutukset siirtotiedostoon
       |      operationId: SiirtotiedostoKoutaLightKoulutukset
       |      description: "Tallenna annettujen koulutusten tiedot. Päivittää aiemmin lisätyt koulutukset ja lisää puuttuvat.
       |        Päivitys tapahtuu koulutuksen externalId-kentän perusteella: jos tietokannasta löytyy koulutusta lisäävän organisaation koulutus samalla externalId:llä,
       |        koulutusta yritetään päivittää, muuten sellainen lisätään."
       |      tags:
       |        - KoutaLightSiirtotiedosto
       |      parameters:
       |        - in: query
       |          name: startTime
       |          schema:
       |            type: string
       |            format: date-time
       |          required: false
       |          example: '$DateTimeExample'
       |        - in: query
       |          name: endTime
       |          schema:
       |            type: string
       |            format: date-time
       |          required: false
       |          description: Jos arvoa ei ole annettu, asetetaan loppuajaksi nykyinen ajankohta.
       |          example: '$DateTimeExample'
       |      responses:
       |        '200':
       |          description: Ok
       |        '401':
       |          description: Unauthorized
       |        '403':
       |          description: Forbidden
       |""".stripMargin
  )
  get("/kouta-light-koulutukset") {
    implicit val authenticated: Authenticated = authenticate
    val isOphPaakayttaja                      = authenticated.session.roles.contains(Role.Paakayttaja)
    val userOid                               = authenticated.session.personOid

    val siirtotiedostoCreationEnabled =
      KoutaConfigurationFactory.configuration.securityConfiguration.transferFileCreationEnabled

    if (siirtotiedostoCreationEnabled) {
      if (isOphPaakayttaja) {
        val (startTime, endTime) = parseTimeRange(params.get("startTime"), params.get("endTime"))
        val result = resultMap(koutaLightSiirtotiedostoService.storeKoulutukset(UUID.randomUUID(), startTime, endTime))

        logger.info(s"Siirtotiedosto-operaatio ajettiin onnistuneesti: ${writePretty(result)}")
        Ok(result)
      } else {
        val errorMsg = Map("error" -> s"Käyttäjällä $userOid ei ole oikeutta koulutusten tallentamiseen rajapinnan kautta")
        logger.warn(errorMsg.toString())
        Forbidden(errorMsg)
      }
    } else {
      val errorMsg = Map("error" -> "Rajapinta ei ole käytössä tässä ympäristössä.")
      logger.warn(errorMsg.toString())
      Forbidden(errorMsg)
    }
  }
}
