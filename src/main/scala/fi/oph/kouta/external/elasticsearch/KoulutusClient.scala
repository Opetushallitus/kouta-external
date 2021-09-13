package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.indexed.KoulutusIndexed
import fi.oph.kouta.domain.oid.KoulutusOid
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KoulutusClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "koulutus-kouta"

  def getKoulutus(oid: KoulutusOid): Future[Koulutus] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[KoulutusIndexed])
      .map(_.toKoulutus)
}

object KoulutusClient extends KoulutusClient(ElasticsearchClient.client)
