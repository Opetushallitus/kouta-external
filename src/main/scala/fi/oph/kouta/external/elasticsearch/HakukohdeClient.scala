package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.{BoolQuery, ExistsQuery, Query, QueryBuilders, TermQuery, TermsQuery, TermsQueryField}
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.domain.oid.{HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.indexed.{HakukohdeIndexed, HakukohdeJavaClient}
import fi.oph.kouta.external.service.HakukohdeSearchParams
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.collection.JavaConverters._

class HakukohdeClient(val client: ElasticClient, val clientJava: elasticsearch.ElasticsearchClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "hakukohde-kouta"

  def getHakukohde(oid: HakukohdeOid): Future[(Hakukohde, Seq[OrganisaatioOid])] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[HakukohdeIndexed])
      .map(h => (h.toHakukohde(None), h.tarjoajat))

  def search(searchParams: HakukohdeSearchParams, hakukohdeOids: Option[Set[HakukohdeOid]]): Future[Seq[Hakukohde]] = {
    val hakuQuery = searchParams.hakuOid.map(oid => TermQuery.of(m => m.field("hakuOid.keyword").value(oid.toString))._toQuery())
    val johtaaTutkintoonQuery = searchParams.johtaaTutkintoon.map(johtaaTutkintoon => TermQuery.of(m => m.field("johtaaTutkintoon").value(johtaaTutkintoon))._toQuery())
    val alkamiskausiQuery = searchParams.alkamiskausi.map(kausi => TermQuery.of(m => m.field("paateltyAlkamiskausi.kausiUri").value(kausi))._toQuery())
    val alkamisvuosiQuery = searchParams.alkamisvuosi.map(vuosi => TermQuery.of(m => m.field("paateltyAlkamiskausi.vuosi").value(vuosi))._toQuery())

    val qQuery =
      searchParams.q.map(q =>
        QueryBuilders.bool.should(
          TermQuery.of(m => m.field("nimi.fi").value(q))._toQuery(),
          TermQuery.of(m => m.field("nimi.sv").value(q))._toQuery(),
          TermQuery.of(m => m.field("nimi.en").value(q))._toQuery(),
          TermQuery.of(m => m.field("jarjestyspaikka.nimi.fi").value(q))._toQuery(),
          TermQuery.of(m => m.field("jarjestyspaikka.nimi.sv").value(q))._toQuery(),
          TermQuery.of(m => m.field("jarjestyspaikka.nimi.en").value(q))._toQuery(),
          TermQuery.of(m => m.field("toteutus.tarjoajat.nimi.fi").value(q))._toQuery(),
          TermQuery.of(m => m.field("toteutus.tarjoajat.nimi.sv").value(q))._toQuery(),
          TermQuery.of(m => m.field("toteutus.tarjoajat.nimi.en").value(q))._toQuery()
        ).build._toQuery()
      )

    val hakutapaQuery = searchParams.hakutapa.map(t =>
      TermsQuery.of(m => m.field("hakutapaKoodiUri").terms(
        new TermsQueryField.Builder()
          .value(searchParams.hakutapa.getOrElse(Set()).toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())

    val koulutusasteQuery = searchParams.koulutusaste.map(aste =>
      TermsQuery.of(m => m.field("koulutusasteKoodiUrit").terms(
        new TermsQueryField.Builder()
          .value(searchParams.koulutusaste.getOrElse(Set()).toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())

    val opetuskieletQuery = searchParams.opetuskieli.map(aste =>
      TermsQuery.of(m => m.field("opetuskieliKoodiUrit").terms(
        new TermsQueryField.Builder()
          .value(searchParams.opetuskieli.getOrElse(Set()).toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())
    val hakukohdeQuery = hakukohdeOids.map(aste =>
      TermsQuery.of(m => m.field("oid.keyword").terms(
        new TermsQueryField.Builder()
          .value(hakukohdeOids.getOrElse(Set()).toList.map(m => FieldValue.of(m.toString)).asJava)
          .build()
      ))._toQuery())
    val tilaQuery = searchParams.tila.map(tila =>
      TermsQuery.of(m => m.field("tila.keyword").terms(
        new TermsQueryField.Builder()
          .value(searchParams.tila.getOrElse(Set()).toList.map(m => FieldValue.of(m.toString)).asJava)
          .build()
      ))._toQuery())

    val tarjoajaQuery =
      searchParams.tarjoajaOids.map(oids =>
        QueryBuilders.bool.should(
          oids.map(oid =>
            QueryBuilders.bool.should(
              TermsQuery.of(m => m.field("jarjestyspaikka.oid.keyword").terms(
                new TermsQueryField.Builder()
                  .value(List(FieldValue.of(oid.toString())).asJava)
                  .build()
              ))._toQuery(),
              QueryBuilders.bool()
                .must(
                  TermsQuery.of(m => m.field("toteutus.tarjoajat.oid.keyword").terms(
                    new TermsQueryField.Builder()
                      .value(List(FieldValue.of(oid.toString())).asJava)
                      .build()))._toQuery())
                .mustNot(ExistsQuery.of(m => m.field("jarjestyspaikka"))._toQuery()).build()._toQuery()
            ).build()._toQuery()).toList.asJava).build()._toQuery())

    val queryList = List(
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
    ).flatten.asJava

    val searchResult =
      searchItems[HakukohdeJavaClient](queryList)
    Future(searchResult.map(_.toResult()).toSeq.map(_.toHakukohde(None)))
  }
}

object HakukohdeClient extends HakukohdeClient(ElasticsearchClient.client, ElasticsearchClient.clientJava)
