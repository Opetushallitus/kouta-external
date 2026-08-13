package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import com.sksamuel.elastic4s.ElasticClient
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed
import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.util.KoutaHitReader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ToteutusClient(val client: ElasticClient, val clientJava: elasticsearch.ElasticsearchClient) extends ElasticsearchClient with KoutaHitReader {
  val index: String = "toteutus-kouta"
  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[ToteutusIndexed])
      .map(_.toToteutus)
}

object ToteutusClient extends ToteutusClient(ElasticsearchClient.client, ElasticsearchClient.clientJava)
