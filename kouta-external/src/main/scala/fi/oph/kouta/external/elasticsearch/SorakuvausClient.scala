package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import com.sksamuel.elastic4s.ElasticClient

import java.util.UUID
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.domain.indexed.SorakuvausIndexed
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SorakuvausClient(val client: ElasticClient, val clientJava: elasticsearch.ElasticsearchClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "sorakuvaus-kouta"
  def getSorakuvaus(id: UUID): Future[Sorakuvaus] =
    getItem(id.toString)
      .map(debugJson)
      .map(_.to[SorakuvausIndexed])
      .map(_.toSorakuvaus)
}

object SorakuvausClient extends SorakuvausClient(ElasticsearchClient.client, ElasticsearchClient.clientJava)
