package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{RichFuture, clusterHealth}
import com.sksamuel.elastic4s.ElasticDsl.ClusterHealthHandler
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.requests.cluster.ClusterHealthResponse
import fi.oph.kouta.external.domain.enums.ElasticsearchHealthStatus
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization.write

import scala.concurrent.ExecutionContext.Implicits.global

object ElasticsearchHealth extends ElasticsearchHealth(ElasticsearchClient.client)

class ElasticsearchHealth(client: ElasticClient) extends KoutaJsonFormats with Logging {
  def checkStatus(): ElasticsearchHealthStatus =
    client.execute {
      clusterHealth()
    }.map {
      case e: RequestFailure =>
        logger.error(s"Elasticsearch error: ${write(e.error)}")
        ElasticsearchHealthStatus.Unreachable
      case response: RequestSuccess[ClusterHealthResponse] =>
        ElasticsearchHealthStatus(response.result.status)
    }.await

}
