package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.{Query, QueryBuilders, TermQuery, TermsQuery, TermsQueryField}
//import com.sksamuel.elastic4s.ElasticApi.{must, should, termsQuery}
import com.sksamuel.elastic4s.ElasticClient

import java.time.Instant
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.domain.Tallennettu
import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.indexed.{HakuIndexed, HakuJavaClient}
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.util.TimeUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._

class HakuClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "haku-kouta"

  def getHaku(oid: HakuOid): Future[(Haku, Instant)] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[HakuIndexed])
      .map(_.toHaku())
      .map(h => (h, TimeUtils.localDateTimeToInstant(h.modified.get.value)))

  def findByOids(hakuOids: Set[HakuOid]): Future[Seq[Haku]] = {
    //  val hakukohteetQuery = should(termsQuery("oid", hakuOids.map(_.toString)))

    val hakukohteetQuery =

      QueryBuilders.bool.should(
        TermsQuery.of(m => m.field("oid").terms(
          new TermsQueryField.Builder()
            .value(hakuOids.toList.map(m => FieldValue.of(m.toString)).asJava)
            .build()
        ))._toQuery()
      ).build()._toQuery()
    //)
    val queryList = List(hakukohteetQuery).asJava

    //    searchItems[HakuIndexed](Some(must(hakukohteetQuery))).map(_.map(_.toHaku()))

    val searchResult = searchItems[HakuJavaClient](queryList)
    Future(searchResult.map(_.toResult()).toSeq.map(_.toHaku()))


  }

  private def byTarjoajaAndTila(tarjoajaOids: Option[Set[OrganisaatioOid]], haku: HakuIndexed): Boolean =
    tarjoajaOids.fold(true)(oids =>
      haku.hakukohteet.exists(hakukohde => {
        hakukohde.tila match {
          case Tallennettu => false
          case _ =>
            hakukohde.jarjestyspaikka.fold(hakukohde.toteutus.tarjoajat.exists(t => oids.contains(t.oid)))(j =>
              oids.contains(j.oid)
            )
        }
      })
    )


  def search(
              ataruId: Option[String],
              tarjoajaOids: Option[Set[OrganisaatioOid]],
              vuosi: Option[Int]
            ): Future[Seq[Haku]] = {
    //    val ataruIdQuery      = ataruId.map(termsQuery("hakulomakeAtaruId.keyword", _))
    val ataruIdQuery = ataruId.map(t =>
      TermsQuery.of(m => m.field("hakulomakeAtaruId.keyword").terms(
        new TermsQueryField.Builder()
          .value(ataruId.toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())
    //val alkamisvuosiQuery = vuosi.map(termsQuery("metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi", _))
    val alkamisvuosiQuery = vuosi.map(t =>
      TermsQuery.of(m => m.field("metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi").terms(
        new TermsQueryField.Builder()
          .value(vuosi.toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())

    //val hakuvuosiQuery    = vuosi.map(termsQuery("hakuvuosi", _))
    val hakuvuosiQuery = vuosi.map(t =>
      TermsQuery.of(m => m.field("hakuvuosi").terms(
        new TermsQueryField.Builder()
          .value(vuosi.toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())

    /*val tarjoajaQuery = tarjoajaOids.map(oids =>
      should(
        oids.map(oid =>
          should(
            termsQuery("hakukohteet.jarjestyspaikka.oid", oid.toString),
            termsQuery("hakukohteet.toteutus.tarjoajat.oid", oid.toString)
          )
        )
      )
    )*/
    val tarjoajaQuery =
      tarjoajaOids.map(oids =>
        QueryBuilders.bool.should(
          oids.map(oid =>
            QueryBuilders.bool.should(
              TermsQuery.of(m => m.field("hakukohteet.jarjestyspaikka.oid").terms(
                new TermsQueryField.Builder()
                  .value(List(FieldValue.of(oid.toString())).asJava)
                  .build()
              ))._toQuery(),
              TermsQuery.of(m => m.field("hakukohteet.toteutus.tarjoajat.oid").terms(
                new TermsQueryField.Builder()
                  .value(List(FieldValue.of(oid.toString())).asJava)
                  .build()
              ))._toQuery()
            ).build()._toQuery()).toList.asJava).build()._toQuery())

    val queryList = List(
      ataruIdQuery,
      tarjoajaQuery,
      alkamisvuosiQuery,
      hakuvuosiQuery).flatten.asJava

    Future(searchItems[HakuJavaClient](queryList).map(_.toResult()).toSeq.map(_.toHaku()))

    //val query = ataruIdQuery ++ tarjoajaQuery ++ alkamisvuosiQuery ++ hakuvuosiQuery
    //logger.info("search: query = " + query)
    //searchItems[HakuIndexed](if (query.isEmpty) None else Some(must(query)))



  }

  def createTermsQuery(fieldName: String, values: Option[Object]): Option[Query] = {

    logger.info("values = " + values)
    val v = List(values.get)

    values.map(t =>
      TermsQuery.of(m => m.field("hakulomakeAtaruId.keyword").terms(
        new TermsQueryField.Builder()
          .value(List(values.get).map(m => FieldValue.of(m.toString)).asJava)
          .build()
      ))._toQuery())



  }
}

object HakuClient extends HakuClient(ElasticsearchClient.client)
