package fi.oph.kouta.external.swagger

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
        |  description: "Uusi koulutustarjonta"
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
           |""".stripMargin +
          op.mkString
    }.mkString

    val modelHeader =
      s"""
         |components:
         |  schemas:
         |""".stripMargin

    Seq(header, paths, modelHeader, getModelAnnotations).mkString
  }

  private def getModelAnnotations: String = {
    val reflections = new Reflections("fi.oph.kouta.external")

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
