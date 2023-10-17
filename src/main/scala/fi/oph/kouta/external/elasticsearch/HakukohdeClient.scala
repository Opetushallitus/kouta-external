package fi.oph.kouta.external.elasticsearch

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.{BoolQuery, Query, QueryBuilders, TermQuery, TermsQuery, TermsQueryField}

import java.util
import java.util
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}
import com.sksamuel.elastic4s.ElasticApi.{existsQuery, must, not, should, termQuery, termsQuery}
//import com.sksamuel.elastic4s.ElasticApi.{bool, existsQuery, must, not, termQuery, termsQuery}
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.domain.oid.{HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.indexed.{HakukohdeIndexed, HakukohdeJavaClient}
import fi.oph.kouta.external.service.HakukohdeSearchParams
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
//import collection.mutable._


import scala.collection.JavaConverters._

class HakukohdeClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "hakukohde-kouta"

  def getHakukohde(oid: HakukohdeOid): Future[(Hakukohde, Seq[OrganisaatioOid])] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[HakukohdeIndexed])
      .map(h => (h.toHakukohde(None), h.tarjoajat))

  def searchESJava(searchParams: HakukohdeSearchParams, hakukohdeOids: Option[Set[HakukohdeOid]]): Future[Seq[Hakukohde]] = {

    logger.info("HakukohdeSearchParams = " + searchParams)

    //val tarjoajaOids = searchParams.tarjoajaOids

    // OK alas

    //    val hakuQueryOld = hakuOid.map(oid => termQuery("hakuOid.keyword", oid.toString))
    val hakuOidQuery = searchParams.hakuOid.map(oid => TermQuery.of(m => m.field("hakuOid.keyword").value(oid.toString))._toQuery())

        //val johtaaTutkintoonQuery = searchParams.johtaaTutkintoon.map(termQuery("johtaaTutkintoon", _))
    val johtaaTutkintoonQuery = searchParams.johtaaTutkintoon.map(johtaaTutkintoon => TermQuery.of(m => m.field("johtaaTutkintoon").value(johtaaTutkintoon))._toQuery())

    // OK ylös
    //val tilaQuery = searchParams.tila.map(tilat => termsQuery("tila.keyword", tilat.map(_.toString)))
    val tilaQuery = searchParams.tila.map(tila => TermQuery.of(m => m.field("tila.keyword").value(tila.toString))._toQuery())

    //    val alkamiskausiQuery = searchParams.alkamiskausi.map(termQuery("paateltyAlkamiskausi.kausiUri", _))
    val alkamiskausiQuery = searchParams.alkamiskausi.map(kausi => TermQuery.of(m => m.field("paateltyAlkamiskausi.kausiUri").value(kausi))._toQuery())

    //val alkamisvuosiQuery = searchParams.alkamisvuosi.map(termQuery("paateltyAlkamiskausi.vuosi", _))
    val alkamisvuosiQuery = searchParams.alkamisvuosi.map(vuosi => TermQuery.of(m => m.field("paateltyAlkamiskausi.vuosi").value(vuosi))._toQuery())

    //val hakutapaQuery = searchParams.hakutapa.map(hakutavat => termsQuery("hakutapaKoodiUri", hakutavat))
    //val hakutapaQuery = searchParams.hakutapa.map(hakutapa => TermQuery.of(m => m.field("hakutapaKoodiUri").value(hakutapa.toString()))._toQuery())

   /* val hakutapaQuery = searchParams.hakutapa.map(opetuskielet => TermsQuery.of(m => m.field("hakutapaKoodiUri").terms(
      new TermsQueryField.Builder()
        .value(util.Arrays.asList(searchParams.hakutapa.map(m => FieldValue.of(m.toString())).get)).build()
    ))._toQuery()
    )*/



    //val opetuskieletQuery = searchParams.opetuskieli.map(termsQuery("opetuskieliKoodiUrit", _))
    val opetuskieletQuery = searchParams.opetuskieli.map(opetuskielet => TermsQuery.of(m => m.field("hakutapaKoodiUri").terms(
      new TermsQueryField.Builder()
        .value(util.Arrays.asList(searchParams.hakutapa.map(m => FieldValue.of(m.toString())).get)).build()
    ))._toQuery()
    )


    logger.info("oids = " + searchParams.tarjoajaOids)
    // TODO: Tekeekö saman kuin alla oleva vanha kysely?
    val tarjoajaQuery = searchParams.tarjoajaOids.map(oid =>
      QueryBuilders.bool.must(
        TermQuery.of(m => m.field("jarjestyspaikka.oid.keyword").value(oid.toString))._toQuery()
      )
     //   .mustNot(
     //   TermQuery.of(m => m.field("toteutus.tarjoajat.oid.keyword").value(oid.toString()))._toQuery()
    //  )
      .build()._toQuery()
    )
    val tarjoajaQuery2 = searchParams.tarjoajaOids.map(oid =>
      QueryBuilders.bool.should(
        TermQuery.of(m => m.field("jarjestyspaikka.oid.keyword").value(oid.toString))._toQuery()
      )        .mustNot(
        TermQuery.of(m => m.field("toteutus.tarjoajat.oid.keyword").value(oid.toString()))._toQuery()
      )
      .build()._toQuery()
    )

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
    val set1 = Option(Set("hakutapa_01", "hakutapa_02"))
    val list = set1.toList.map(l => TermQuery.of(m => m.field("nimi.fi")
      .value(l.toString()))._toQuery())
    //val list2 = list.map(l => TermQuery.of(m => m.field("nimi.fi").value(l))._toQuery())
    //set1.toList.map(l => TermQuery.of(m => m.field("nimi.fi").value(l))._toQuery()).asJava

    logger.info("searchParams.hakutapa = " + searchParams.hakutapa)

    val hakutapaList = searchParams.hakutapa.get.toList
    logger.info("searchParams.hakutapa.get.toList = " + searchParams.hakutapa.get.toList)
    val hakutapaSet = searchParams.hakutapa.get
    logger.info("searchParams.hakutapa.get = " + searchParams.hakutapa.get)
    val hakutapaList2 = searchParams.hakutapa.get.map(m => Option.apply(m).get).toList
    logger.info("hakutapaList2 = " + hakutapaList2)
    //val nums2 = Some(Set(1, 2, 3, 4))
    //println("Testing: " + nums.map(m => Option.apply(m).get).toList)

    val hakutapaQuery = Option.apply(QueryBuilders.bool().must(
      searchParams.hakutapa.get.toList.map(
        q => TermQuery.of(m => m.field("hakutapaKoodiUri").value(q)
        )._toQuery())
        .asJava
    ).build()._toQuery())

    val hakutapaQueryEiKaiToimi = Option.apply(
      QueryBuilders.bool()
      .must(searchParams.hakutapa.get.toList
        .map(l => TermQuery.of(m => m.field("hakutapaKoodiUri").value(l.toString()))
          ._toQuery()).asJava).build()._toQuery())
        /*
    val hakutapaQueryBuilder: BoolQuery.Builder = QueryBuilders.bool()
      //.must()
    val v1 = TermQuery.of(m => m.field("hakutapaKoodiUri").value("hakutapa_01"))._toQuery()
    val v2 = TermQuery.of(m => m.field("hakutapaKoodiUri").value("hakutapa_02"))._toQuery()

    val list1 = List(v1, v2).asJava
    hakutapaQueryBuilder.must(list1)
    val hakutapaQuery = Option.apply(hakutapaQueryBuilder.build()._toQuery())*/
    logger.info("hakutapaQuery.build() UUSI = " + hakutapaQuery)
    //   TermsQueryField.of(m => )
    // TermsQuery.of(m => m.field("aa")terms((List(v1, v2)))

    //    for(value : searchParams.hakutapa.va) {}
    //val hakutapaQuery2 = searchParams.hakutapa.map(hakutavat =>
    //  TermQuery.of(m => m.field("hakutapaKoodiUri").value(hakutavat.toString()))._toQuery())
    /*val hakutapaQuery2 = searchParams.hakutapa.foreach(hakutavat =>
      TermsQuery.of(m => m.field("hakutapaKoodiUri"))._toQuery()
    )
    logger.info("hakutapaQuery2 = " + hakutapaQuery2)
*/

    /*val hakutapaQuery = searchParams.hakutapa.map(hakutavat =>
      TermsQuery.of(m => m.field("hakutapaKoodiUri").terms(
        new TermsQueryField.Builder()
          .value(util.Arrays.asList(searchParams.hakutapa.map(m => FieldValue.of(m.toString())).get)).build()
      ))._toQuery())*/
    val queryList = List(
      hakutapaQuery,
   //   tarjoajaQuery,

      //      hakukohdeQuery,

//            tilaQuery,

      //      opetuskieletQuery,

      //      koulutusasteQuery

      // Nää on OK
      hakuOidQuery
      // qQuery,
      //alkamisvuosiQuery,
      //alkamiskausiQuery,
      //johtaaTutkintoonQuery,
    ).flatten.asJava

      //  searchItems[HakukohdeIndexed](Some(must(Option.empty))).map(_.map(_.toHakukohde(None)))
    //searchItemsJavaClient[HakukohdeJavaClient](Some(must(query))).map(_.toResult()).map(_.toHakukohde(None))

    //val searchResult: List[HakukohdeJavaClient] = searchItemsJavaClient[HakukohdeJavaClient](Option.apply(Some(must(query))))
    logger.info("queryList = " + queryList)
    //val searchResult: List[HakukohdeJavaClient] = searchItemsJavaClient[HakukohdeJavaClient](queryList)
    val searchResult =
      searchItemsJavaClient[HakukohdeJavaClient](queryList)
    logger.info("Saatiin yhteensä osumia: " + searchResult.size)
    Future(searchResult.map(_.toResult()).toSeq.map(_.toHakukohde(None)))
//    searchResult.
    //.map(_.toResult()).toSeq.map(_.toHakukohde(None))
    //val searchResult: Future[List[HakukohdeJavaClient]] = searchItemsJavaClient[HakukohdeJavaClient](Option.apply(null))

    //Future(Seq.empty)
  }

  /*
  val values1 =  util.Arrays.asList(searchParams.tila.map(t => FieldValue.of(t.toString())).get)
  logger.info("values1 = " + values1)
      val termsQueryField: TermsQueryField = new TermsQueryField.Builder()
        .value(
          util.Arrays.asList(
            searchParams.tila.map(t => FieldValue.of(t.toString())).get

          )
        ).build()
      val tilaQuery = searchParams.tila.map(tilat => TermsQuery.of(m.apply() => m.field("tila.keyword").terms(
        //termsQueryField
        new TermsQueryField.Builder()
          .value(
            util.Arrays.asList(
              searchParams.tila.map(m => FieldValue.of(m.toString())).get
            )

          ).build()
      ))._toQuery())


  */
  //val alkamiskausiQuery = searchParams.alkamiskausi.map(TermsQuery("paateltyAlkamiskausi.kausiUri", _))
  /* val queryBuilder = QueryBuilders.boolQuery()
   .filter(QueryBuilders.termsQuery("paateltyAlkamiskausi.kausiUri", searchParams.alkamiskausi))
   queryBuilder.filter(QueryBuilders.termsQuery("paateltyAlkamiskausi.vuosi", searchParams.alkamisvuosi))
*/
  /*
  BoolQueryBuilder query = QueryBuilders.boolQuery()
     .filter(QueryBuilders.termsQuery("department", departments))
     .filter(QueryBuilders.termsQuery("job", jobs))
     .filter(QueryBuilders.termsQuery("name", names));
   */
  /*

      val alkamiskausiQuery = searchParams.alkamiskausi.map(termQuery("paateltyAlkamiskausi.kausiUri", _))
      val alkamisvuosiQuery = searchParams.alkamisvuosi.map(termQuery("paateltyAlkamiskausi.vuosi", _))
      val koulutusasteQuery = searchParams.koulutusaste.map(asteet => termsQuery("koulutusasteKoodiUrit", asteet))
      val hakukohdeQuery = hakukohdeOids.map(oids => termsQuery("oid.keyword", oids.map(_.toString)))
    */

  /*val tarjoajaQuery = tarjoajaOids.map(oids =>
    should(
      oids.map(oid =>
        should(
          termsQuery("jarjestyspaikka.oid.keyword", oid.toString),
          not(existsQuery("jarjestyspaikka")).must(termsQuery("toteutus.tarjoajat.oid.keyword", oid.toString))
        )
      )
    )
  )*/
  //val tarjoajaQuery = null

  /*  val tarjoajaQuery = tarjoajaOids.map(oids =>
      TermsQuery.of(m => m.field("jarjestyspaikka.oid.keyword").terms(
        new TermsQueryField.Builder()
          .value(util.Arrays.asList(tarjoajaOids.map(m => FieldValue.of(m.toString())).get)).build()
      )))
*/

  /*should(
    oids.map(oid =>
      should(
        TermsQuery("jarjestyspaikka.oid.keyword", oid.toString),
        not(existsQueshoury("jarjestyspaikka")).must(termsQuery("toteutus.tarjoajat.oid.keyword", oid.toString))
      )
    )
  )

   */
  //)


  def search(searchParams: HakukohdeSearchParams, hakukohdeOids: Option[Set[HakukohdeOid]]): Future[Seq[Hakukohde]] = {
    val hakuOid = searchParams.hakuOid
    val tarjoajaOids = searchParams.tarjoajaOids
    val hakuQuery = hakuOid.map(oid => termQuery("hakuOid.keyword", oid.toString))
    val johtaaTutkintoonQuery = searchParams.johtaaTutkintoon.map(termQuery("johtaaTutkintoon", _))
    val tilaQuery = searchParams.tila.map(tilat => termsQuery("tila.keyword", tilat.map(_.toString)))
    val hakutapaQuery = searchParams.hakutapa.map(hakutavat => termsQuery("hakutapaKoodiUri", hakutavat))
    logger.info("hakutapaQuery VANHA = " + hakutapaQuery)
    val opetuskieletQuery = searchParams.opetuskieli.map(termsQuery("opetuskieliKoodiUrit", _))
    val alkamiskausiQuery = searchParams.alkamiskausi.map(termQuery("paateltyAlkamiskausi.kausiUri", _))
    val alkamisvuosiQuery = searchParams.alkamisvuosi.map(termQuery("paateltyAlkamiskausi.vuosi", _))
    val koulutusasteQuery = searchParams.koulutusaste.map(asteet => termsQuery("koulutusasteKoodiUrit", asteet))
    val hakukohdeQuery = hakukohdeOids.map(oids => termsQuery("oid.keyword", oids.map(_.toString)))
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

    searchItems[HakukohdeIndexed](Some(must(query))).map(_.map(_.toHakukohde(None)))
  }
/*  def searchOldClient(searchParams: HakukohdeSearchParams, hakukohdeOids: Option[Set[HakukohdeOid]]): Future[Seq[Hakukohde]] = {
    logger.info("HakukohdeSearchParams = " + searchParams)
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
    logger.info("oids = " + tarjoajaOids)
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

 //searchItems[HakukohdeIndexed](Some(must(query))).map(_.map(_.toHakukohde(None)))
    //searchItemsJavaClient[HakukohdeJavaClient](Some(must(query))).map(_.toResult()).map(_.toHakukohde(None))

    val searchResult: List[HakukohdeJavaClient] = searchItemsJavaClient[HakukohdeJavaClient](Some(must(query)))
    Future(searchResult.map(_.toResult()).toSeq.map(_.toHakukohde(None)))
//    Future(Seq.empty)
  }

 */
}

object HakukohdeClient extends HakukohdeClient(ElasticsearchClient.client)
