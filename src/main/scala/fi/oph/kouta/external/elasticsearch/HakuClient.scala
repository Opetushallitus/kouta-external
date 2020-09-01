package fi.oph.kouta.external.elasticsearch

import java.time.Instant
import java.util.UUID

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.indexed.HakuIndexed
import fi.oph.kouta.domain.oid.HakuOid
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.util.TimeUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "haku-kouta"

  def getHaku(oid: HakuOid): Future[(Haku, Instant)] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[HakuIndexed])
      .map(_.toHaku)
      .map(h => (h, TimeUtils.localDateTimeToInstant(h.modified.get)))

}

object HakuClient extends HakuClient(ElasticsearchClient.client)
