package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{existsQuery, must, not, should, termsQuery}
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "hakukohde-kouta"

  def getHakukohde(oid: HakukohdeOid): Future[(Hakukohde, Seq[OrganisaatioOid])] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[HakukohdeIndexed])
      .map(h => (h.toHakukohde(None), h.tarjoajat))

  def search(
              hakuOid: Option[HakuOid],
              tarjoajaOids: Option[Set[OrganisaatioOid]],
              q: Option[String],
              hakukohdeOids: Option[Set[HakukohdeOid]]
            ): Future[Seq[Hakukohde]] = {
    val hakuQuery = hakuOid.map(oid => termsQuery("hakuOid", oid.toString))
    val hakukohdeQuery = hakukohdeOids.map(oids => should(oids.map(oid => must(termsQuery("oid", oid.toString())))))
    val tarjoajaQuery = tarjoajaOids.map(oids =>
      should(
        oids.map(oid =>
          should(
            termsQuery("jarjestyspaikka.oid", oid.toString),
            not(existsQuery("jarjestyspaikka")).must(termsQuery("toteutus.tarjoajat.oid", oid.toString))
          )
        )
      )
    )
    val qQuery = q.map(q =>
      should(
        termsQuery("nimi.fi.keyword", q),
        termsQuery("nimi.sv.keyword", q),
        termsQuery("nimi.en.keyword", q),
        termsQuery("jarjestyspaikka.nimi.fi.keyword", q),
        termsQuery("jarjestyspaikka.nimi.sv.keyword", q),
        termsQuery("jarjestyspaikka.nimi.en.keyword", q),
        termsQuery("toteutus.tarjoajat.nimi.fi.keyword", q),
        termsQuery("toteutus.tarjoajat.nimi.sv.keyword", q),
        termsQuery("toteutus.tarjoajat.nimi.en.keyword", q)
      )
    )
    searchItems[HakukohdeIndexed](Some(must(hakuQuery ++ tarjoajaQuery ++ qQuery ++ hakukohdeQuery)))
      .map(_.map(_.toHakukohde(None)))
  }
}

object HakukohdeClient extends HakukohdeClient(ElasticsearchClient.client)
