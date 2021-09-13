package fi.oph.kouta.external.servlet

import com.sksamuel.elastic4s.ElasticClient
import fi.oph.kouta.external.domain.enums.ElasticsearchHealthStatus.{Green, Red, Unreachable, Yellow}
import fi.oph.kouta.external.elasticsearch.{ElasticsearchClient, ElasticsearchHealth}
import fi.oph.kouta.external.kouta.CasKoutaClient
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import org.scalatra._

object HealthcheckServlet extends HealthcheckServlet(ElasticsearchClient.client)

class HealthcheckServlet(client: ElasticClient) extends KoutaServlet {

  registerPath(
    "/healthcheck/",
    s"""    get:
       |      summary: Healthcheck-rajapinta
       |      description: Healthcheck-rajapinta
       |      tags:
       |        - Admin
       |      responses:
       |        '200':
       |          description: Ok
       |""".stripMargin
  )
  get("/") {
    Ok("message" -> "ok")
  }

  registerPath(
    "/healthcheck/elastic",
    s"""    get:
       |      summary: Tarkista yhteys Elasticsearchiin
       |      description: Tarkista yhteys Elasticsearchiin
       |      tags:
       |        - Admin
       |      responses:
       |        '200':
       |          description: Ok
       |""".stripMargin
  )
  get("/elastic") {
    ElasticsearchHealth.checkStatus() match {
      case s if s == Unreachable || s == Red =>
        InternalServerError("status" -> s.name)
      case s if s == Yellow || s == Green =>
        Ok("status" -> s.name)
    }
  }

  registerPath(
    "/healthcheck/kouta",
    s"""    get:
       |      summary: Tarkista yhteys Kouta-backendiin
       |      description: Tarkista yhteys Kouta-backendiin
       |      tags:
       |        - Admin
       |      responses:
       |        '200':
       |          description: Ok
       |""".stripMargin
  )
  get("/kouta") {
    if (CasKoutaClient.session()) {
      Ok("message" -> "ok")
    } else {
      InternalServerError("message" -> "error")
    }
  }
}
