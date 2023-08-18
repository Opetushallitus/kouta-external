package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{bool, existsQuery, must, not, should, termQuery, termsQuery}
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
    val johtaaTutkintoonQuery = searchParams.johtaaTutkintoon.map(termQuery("johtaaTutkintoon", _))
    val tilaQuery             = searchParams.tila.map(tilat => termsQuery("tila.keyword", tilat.map(_.toString)))
    val hakutapaQuery         = searchParams.hakutapa.map(hakutavat => termsQuery("hakutapaKoodiUri", hakutavat))
    val opetuskieletQuery     = searchParams.opetuskieli.map(termsQuery("opetuskieliKoodiUrit", _))
    val alkamiskausiQuery     = searchParams.alkamiskausi.map(termQuery("paateltyAlkamiskausi.kausiUri", _))
    val alkamisvuosiQuery     = searchParams.alkamisvuosi.map(termQuery("paateltyAlkamiskausi.vuosi", _))
    val koulutusasteQuery     = searchParams.koulutusaste.map(asteet => termsQuery("koulutusasteKoodiUrit", asteet))
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
        termQuery("nimi.fi", q),
        termQuery("nimi.sv", q),
        termQuery("nimi.en", q),
        termQuery("jarjestyspaikka.nimi.fi", q),
        termQuery("jarjestyspaikka.nimi.sv", q),
        termQuery("jarjestyspaikka.nimi.en", q),
        termQuery("toteutus.tarjoajat.nimi.fi", q),
        termQuery("toteutus.tarjoajat.nimi.sv", q),
        termQuery("toteutus.tarjoajat.nimi.en", q)
      )
    )
    val query = List(
        hakuQuery,
        tarjoajaQuery,
        qQuery,
        hakukohdeQuery,
        johtaaTutkintoonQuery,
        tilaQuery,
        hakutapaQuery,
        opetuskieletQuery,
        alkamiskausiQuery,
        alkamisvuosiQuery,
        koulutusasteQuery
      ).flatten

    searchItemsSearchAfter[HakukohdeIndexed](Some(must(query))).map(_.map(_.toHakukohde(None)))
  }
}

object HakukohdeClient extends HakukohdeClient(ElasticsearchClient.client)
