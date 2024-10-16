package fi.oph.kouta.external.swagger

import fi.oph.kouta.external.servlet.KoutaServlet
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
        |openapi: 3.0.3
        |info:
        |  title: kouta-external
        |  version: 0.2-SNAPSHOT
        |  description: >
        |    <p><strong>Uuden koulutustarjonnan Opintopolun ulkopuolisille palveluille tarkoitetut rajapinnat.</strong></p>
        |    <p>Ohjeita rajapintojen kutsujalle:
        |    <ul>
        |    <li><a href="https://wiki.eduuni.fi/x/L95cCw" target="_blank">Autentikaatio</a></li>
        |    <li><a href="https://wiki.eduuni.fi/x/0MeKCg" target="_blank">Kutsujan tunniste (caller-id)</a></li>
        |    <li><a href="https://wiki.eduuni.fi/x/EL_hCw" target="_blank">CSRF-suojaus</a></li>
        |    </ul>
        |    </p>
        |    <p>Helpoin tapa kirjautua sisään Swagger-ui:n käyttäjälle on avata
        |    <a href="/kouta-external/auth/login" target="_blank">/kouta-external/auth/login</a> uuteen selainikkunaan.
        |    Jos näkyviin tulee <code>{"personOid":"1.2.246.562.24.xxxx"}</code> on kirjautuminen onnistunut. Jos näkyviin tulee
        |    opintopolun kirjautumisikkuna, kirjaudu sisään.</p>
        |  termsOfService: https://opintopolku.fi/wp/fi/opintopolku/tietoa-palvelusta/
        |  contact:
        |    name: "Opetushallitus"
        |    email: verkkotoimitus_opintopolku@oph.fi
        |    url: "https://www.oph.fi/"
        |  license:
        |    name: "EUPL 1.1 or latest approved by the European Commission"
        |    url: "http://www.osor.eu/eupl/"
        |servers:
        |  - url: /kouta-external/
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
         |      description: Kutsujan <a href="https://wiki.eduuni.fi/pages/viewpage.action?pageId=176867280">Caller ID</a>
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
