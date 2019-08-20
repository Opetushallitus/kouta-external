package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.indexed.KoulutusIndexed
import fi.oph.kouta.external.domain.oid.KoulutusOid
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KoulutusClient extends KoulutusClient("koulutus-kouta")

abstract class KoulutusClient(override val index: String)
    extends ElasticsearchClient(index, "koulutus")
    with KoutaJsonFormats {

  def getKoulutus(oid: KoulutusOid): Future[Koulutus] =
    getItem(oid.s)
      .map(_.to[KoulutusIndexed])
      .map(_.toKoulutus)
}