package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{existsQuery, must, not, should, termQuery, termsQuery}
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.domain.oid.{HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.external.service.HakukohdeSearchParams
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

  def search(searchParams: HakukohdeSearchParams, hakukohdeOids: Option[Set[HakukohdeOid]]): Future[Seq[Hakukohde]] = {
    val hakuOid               = searchParams.hakuOid
    val tarjoajaOids          = searchParams.tarjoajaOids
    val hakuQuery             = hakuOid.map(oid => termQuery("hakuOid.keyword", oid.toString))
    val tilaQuery             = searchParams.tila.map(tilat => termsQuery("tila.keyword", tilat.map(_.toString)))
    val johtaaTutkintoonQuery = searchParams.johtaaTutkintoon.map(termQuery("johtaaTutkintoon", _))
    val hakutapaQuery         = searchParams.hakutapa.map(termsQuery("hakutapaKoodiUri.keyword", _))
    val opetuskieletQuery     = searchParams.opetuskieli.map(termsQuery("opetuskieliKoodiUrit.keyword", _))
    val alkamiskausiQuery     = searchParams.alkamiskausi.map(termQuery("paateltyAlkamiskausi.kausiUri.keyword", _))
    val alkamisvuosiQuery     = searchParams.alkamisvuosi.map(termQuery("paateltyAlkamiskausi.vuosi.keyword", _))
    val koulutusasteQuery     = searchParams.koulutusaste.map(termsQuery("koulutusasteKoodiUrit.keyword", _))
    val hakukohdeQuery        = hakukohdeOids.map(oids => termsQuery("oid.keyword", oids.map(_.toString)))
    val tarjoajaQuery = tarjoajaOids.map(oids =>
      should(
        oids.map(oid =>
          should(
            termsQuery("jarjestyspaikka.oid.keyword", oid.toString),
            not(existsQuery("jarjestyspaikka")).must(termsQuery("toteutus.tarjoajat.oid.keyword", oid.toString))
          )
        )
      )
    )
    val qQuery = searchParams.q.map(q =>
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
    val query = must(
      hakuQuery ++ tarjoajaQuery ++ qQuery ++ hakukohdeQuery ++ tilaQuery ++ johtaaTutkintoonQuery ++ hakutapaQuery ++
        opetuskieletQuery ++ alkamiskausiQuery ++ alkamisvuosiQuery ++ koulutusasteQuery
    )
    searchItems[HakukohdeIndexed](Some(query)).map(_.map(_.toHakukohde(None)))
  }
}

object HakukohdeClient extends HakukohdeClient(ElasticsearchClient.client)
