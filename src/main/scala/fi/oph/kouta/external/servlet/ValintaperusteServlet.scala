package fi.oph.kouta.external.servlet

import java.util.UUID

import fi.oph.kouta.external.elasticsearch.ElasticsearchClientHolder
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.ValintaperusteService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class ValintaperusteServlet(elasticsearchClientHolder: ElasticsearchClientHolder)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  val valintaperusteService = new ValintaperusteService(elasticsearchClientHolder)

  registerPath(
    "/valintaperuste/{id}",
    """    get:
      |      summary: Hae valintaperustekuvauksen tiedot
      |      operationId: Hae valintaperuste
      |      description: Hakee valintaperustekuvauksen kaikki tiedot
      |      tags:
      |        - Valintaperuste
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Valintaperuste-id
      |          example: ea596a9c-5940-497e-b5b7-aded3a2352a7
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Valintaperuste'
      |""".stripMargin
  )
  get("/:id") {
    implicit val authenticated: Authenticated = authenticate

    valintaperusteService.get(UUID.fromString(params("id")))
  }
}
