package fi.oph.kouta.external.elasticsearch

import java.util.UUID

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.domain.indexed.ValintaperusteIndexed
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValintaperusteClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "valintaperuste-kouta"

  def getValintaperuste(id: UUID): Future[Valintaperuste] =
    getItem(id.toString)
      .map(_.to[ValintaperusteIndexed])
      .map(_.toValintaperuste)
}

object ValintaperusteClient extends ValintaperusteClient(ElasticsearchClient.client)
