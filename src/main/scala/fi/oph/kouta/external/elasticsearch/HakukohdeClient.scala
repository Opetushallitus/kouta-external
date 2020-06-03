package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.domain.oid.{HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "hakukohde-kouta"

  def getHakukohde(oid: HakukohdeOid): Future[(Hakukohde, Seq[OrganisaatioOid])] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[HakukohdeIndexed])
      .map(h => (h.toHakukohde, h.tarjoajat))
}

object HakukohdeClient extends HakukohdeClient(ElasticsearchClient.client)
