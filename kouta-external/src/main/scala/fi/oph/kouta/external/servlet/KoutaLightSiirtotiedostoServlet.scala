package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import fi.oph.kouta.koutalight.SiirtotiedostoApp.SiirtotiedostoInstantFormat
import fi.oph.kouta.koutalight.client.SiirtotiedostoPalveluClient
import fi.oph.kouta.koutalight.repository.{KoutaDatabaseConnection, KoutaLightSiirtotiedostoDAO}
import fi.oph.kouta.koutalight.service.{KoutaLightSiirtotiedostoService, SiirtotiedostoOperationResults}
import fi.oph.kouta.servlet.Authenticated
import org.scalatra.Ok

import java.time.{Instant, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.util.{Failure, Success, Try}

object KoutaLightSiirtotiedostoServlet extends KoutaLightSiirtotiedostoServlet

class KoutaLightSiirtotiedostoServlet extends KoutaServlet with CasAuthenticatedServlet {
  private val dbConnectionConfiguration =
    KoutaConfigurationFactory.configuration.ovaraKoutaLightConfiguration.databaseConnectionConfiguration
  private val s3Configuration = KoutaConfigurationFactory.configuration.ovaraKoutaLightConfiguration.s3Configuration
  private val dbConnection    = KoutaDatabaseConnection(dbConnectionConfiguration)

  private val koutaLightSiirtotiedostoDAO           = new KoutaLightSiirtotiedostoDAO(dbConnection)
  private val koutaLightSiirtotiedostoPalveluClient = new SiirtotiedostoPalveluClient(s3Configuration)
  private val koutaLightSiirtotiedostoService: KoutaLightSiirtotiedostoService =
    new KoutaLightSiirtotiedostoService(koutaLightSiirtotiedostoDAO, koutaLightSiirtotiedostoPalveluClient)

  private def parseInstant(
      dateTime: Option[String],
      fieldName: String
  ): Option[Instant] = {
    dateTime match {
      case Some(dateTimeStr) =>
        Try[Instant] {
          Instant.from(SiirtotiedostoInstantFormat.parse(dateTimeStr))
        } match {
          case Success(instant) => Some(instant)
          case Failure(_)       => throw new IllegalArgumentException(s"Virheellinen $fieldName '$dateTimeStr'")
        }
      case None => None
    }
  }

  private def parseTimeRange(
      startTime: Option[String],
      endTime: Option[String] = None
  ): (Option[Instant], Instant) = {
    val defaultEndTime = Instant.now()
    val startTimeVal   = parseInstant(startTime, "alkuaika")
    val endTimeVal     = parseInstant(endTime, "loppuaika").getOrElse(defaultEndTime)
    (startTimeVal, endTimeVal) match {
      case (Some(startTimeVal), _) if startTimeVal.isAfter(Instant.now()) =>
        throw new IllegalArgumentException("Alkuaika ei voi olla tulevaisuudessa")
      case (Some(startTimeVal), endTimeVal) if startTimeVal.isAfter(endTimeVal) =>
        throw new IllegalArgumentException("Alkuaika ei voi olla loppuajan jälkeen")
      case (_, _) => (startTimeVal, endTimeVal)
    }
  }

  private def resultMap(operationResults: SiirtotiedostoOperationResults) =
    Map(
      "keys"    -> operationResults.s3ObjectKeys.mkString(", "),
      "count"   -> operationResults.count.toString,
      "success" -> "true"
    )

  private val SiirtotiedostoDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
  private val DateTimeExample              = SiirtotiedostoDateTimeFormat.format(LocalDateTime.now())
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
    implicit val _: Authenticated = authenticate
    val (startTime, endTime)      = parseTimeRange(params.get("startTime"), params.get("endTime"))

    Ok(resultMap(koutaLightSiirtotiedostoService.storeKoulutukset(UUID.randomUUID(), startTime, endTime)))
  }
}
