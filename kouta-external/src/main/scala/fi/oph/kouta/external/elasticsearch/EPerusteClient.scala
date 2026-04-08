package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits.Json4sHitReader
import fi.oph.kouta.domain.oid.GenericOid
import fi.oph.kouta.external.domain.indexed.{EPeruste, EPerusteIndexed}
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EPerusteClient(val client: ElasticClient, val clientJava: elasticsearch.ElasticsearchClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "eperuste"
  def getEPeruste(oid: GenericOid): Future[EPeruste] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[EPerusteIndexed])
      .map(_.toEPeruste)
}

object EPerusteClient extends EPerusteClient(ElasticsearchClient.client, ElasticsearchClient.clientJava)