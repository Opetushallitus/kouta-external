package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed
import fi.oph.kouta.external.domain.oid.ToteutusOid
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ToteutusClient(override val index: String, elasticsearchClientHolder: ElasticsearchClientHolder)
  extends ElasticsearchClient(index, "toteutus", elasticsearchClientHolder)
    with KoutaJsonFormats {

  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    getItem(oid.s)
      .map(_.to[ToteutusIndexed])
      .map(_.toToteutus)
}
