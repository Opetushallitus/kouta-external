package fi.oph.kouta.external.elasticsearch

import java.util.UUID

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.indexed.HakuIndexed
import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuClient(override val index: String, elasticsearchClientHolder: ElasticsearchClientHolder)
  extends ElasticsearchClient(index, "haku", elasticsearchClientHolder)
    with KoutaJsonFormats {

  def getHaku(oid: HakuOid): Future[Haku] =
    getItem(oid.s)
      .map(_.to[HakuIndexed])
      .map(_.toHaku)

  def searchByAtaruId(id: UUID): Future[Seq[Haku]] =
    simpleSearch("hakulomakeAtaruId", id.toString)
      .map(_.to[HakuIndexed])
      .map(_.map(_.toHaku))
}
