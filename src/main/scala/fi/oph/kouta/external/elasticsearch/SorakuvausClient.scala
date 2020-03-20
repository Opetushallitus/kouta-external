package fi.oph.kouta.external.elasticsearch

import java.util.UUID

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Sorakuvaus
import fi.oph.kouta.external.domain.indexed.SorakuvausIndexed
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SorakuvausClient(override val index: String, elasticsearchClientHolder: ElasticsearchClientHolder)
  extends ElasticsearchClient(index, "sorakuvaus", elasticsearchClientHolder)
    with KoutaJsonFormats
    with Logging {

  def getSorakuvaus(id: UUID): Future[Sorakuvaus] =
    getItem(id.toString)
      .map(_.to[SorakuvausIndexed])
      .map(_.toSorakuvaus)
}
