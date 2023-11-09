package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed
import fi.oph.kouta.domain.oid.ToteutusOid
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ToteutusClient(val client: ElasticClient, val clientJava: elasticsearch.ElasticsearchClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "toteutus-kouta"
  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[ToteutusIndexed])
      .map(_.toToteutus)
}

object ToteutusClient extends ToteutusClient(ElasticsearchClient.client, ElasticsearchClient.clientJava)
