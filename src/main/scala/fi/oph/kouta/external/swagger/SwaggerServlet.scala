package fi.oph.kouta.external.swagger

import fi.oph.kouta.external.servlet.KoutaServlet
import fi.oph.kouta.swagger.SwaggerModel
import org.reflections.Reflections
import org.scalatra.ScalatraServlet

import scala.collection.JavaConverters._

class SwaggerServlet extends ScalatraServlet {

  get("/swagger.yaml") {
    response.setContentType("text/yaml")
    renderOpenapi3Yaml
  }

  protected lazy val renderOpenapi3Yaml: String = {
    val header =
      """
        |openapi: 3.0.0
        |info:
        |  title: kouta-external
        |  description: >
        |    Uuden koulutustarjonnan ulkoinen API.
        |
        |
        |    Ohjeet kirjautumiseen rajapintojen kutsujalle:
        |    [https://confluence.csc.fi/display/oppija/Rajapintojen+autentikaatio](https://confluence.csc.fi/display/oppija/Rajapintojen+autentikaatio)
        |
        |
        |    Helpoin tapa kirjautua sisään Swagger-ui:n käyttäjälle on avata
        |    [/kouta-external/auth/login](/kouta-external/auth/login) uuteen selainikkunaan.
        |    Jos näkyviin tulee `{"personOid":"1.2.246.562.24.xxxx"}` on kirjautuminen onnistunut. Jos näkyviin tulee
        |    opintopolun kirjautumisikkuna, kirjaudu sisään.
        |  version: 0.1-SNAPSHOT
        |  termsOfService: https://opintopolku.fi/wp/fi/opintopolku/tietoa-palvelusta/
        |  contact:
        |    name: ""
        |    email: verkkotoimitus_opintopolku@oph.fi
        |    url: ""
        |  license:
        |    name: "EUPL 1.1 or latest approved by the European Commission"
        |    url: "http://www.osor.eu/eupl/"
        |servers:
        |  - url: /kouta-external/
        |  - url: http://localhost:8097/kouta-external/
        |  - url: https://virkailija.untuvaopintopolku.fi/kouta-external/
        |  - url: https://virkailija.hahtuvaopintopolku.fi/kouta-external/
        |  - url: https://virkailija.testiopintopolku.fi/kouta-external/
        |  - url: https://virkailija.opintopolku.fi/kouta-external/
        |paths:
        |""".stripMargin

    val paths = SwaggerPaths.paths.map {
      case (path, op) =>
        s"""  $path:
           |    parameters:
           |      - $$ref: '#/components/parameters/callerId'
           |""".stripMargin +
          op.mkString
    }.mkString

    val componentsHeader =
      s"""
         |components:
         |  parameters:
         |    xIfUnmodifiedSince:
         |      in: header
         |      name: ${KoutaServlet.IfUnmodifiedSinceHeader}
         |      schema:
         |        type: string
         |        default: ${KoutaServlet.SampleHttpDate}
         |      required: true
         |      description: Vastaavan GETin ${KoutaServlet.LastModifiedHeader}
         |    callerId:
         |      in: header
         |      name: Caller-Id
         |      schema:
         |        type: string
         |        default: kouta-external-swagger
         |      required: true
         |      description: Kutsujan <a href="https://confluence.csc.fi/pages/viewpage.action?pageId=50858064">Caller ID</a>
         |  schemas:
         |""".stripMargin

    Seq(header, paths, componentsHeader, getModelAnnotations).mkString
  }

  private def getModelAnnotations: String = {
    val reflections = new Reflections("fi.oph.kouta")

    reflections
      .getTypesAnnotatedWith(classOf[SwaggerModel])
      .asScala
      .toSeq
      .sortBy(_.getSimpleName)
      .map(_.getAnnotation(classOf[SwaggerModel]))
      .filter(_ != null)
      .map(_.value.stripMargin)
      .mkString
  }
}
