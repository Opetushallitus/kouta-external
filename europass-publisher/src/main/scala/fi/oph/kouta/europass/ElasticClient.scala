package fi.oph.kouta.europass

import org.json4s._
import org.json4s.jackson.JsonMethods.{parse, render, compact}
import org.asynchttpclient.Dsl._
import org.asynchttpclient._

import com.sksamuel.elastic4s.{ElasticClient => ScalaElasticClient}
import co.elastic.clients.elasticsearch._types.query_dsl.{Query, QueryBuilders, MatchQuery}
import co.elastic.clients.elasticsearch.{ElasticsearchClient => JavaElasticClient}

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.domain.indexed.ToteutusIndexed
import fi.oph.kouta.external.elasticsearch.ElasticsearchClient
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.external.{ElasticSearchConfiguration, KoutaConfigurationFactory}

import scala.collection.JavaConverters._

object ElasticQueries {
  def toteutusByOid(oid: String): Query =
    QueryBuilders.bool.must(
      MatchQuery.of(q => q.field("oid.keyword").query(oid))._toQuery()
    ).build()._toQuery()

  def toteutuksetByTila(tila: String): Query =
    QueryBuilders.bool.must(
      MatchQuery.of(q => q.field("tila").query(tila))._toQuery()
    ).build()._toQuery()
}

class ToteutusOps(
  val client: ScalaElasticClient,
  val clientJava: JavaElasticClient
) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "toteutus-kouta"
  override val config: ElasticSearchConfiguration = ElasticSearchConfiguration(
    elasticUrl = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.url"),
    authEnabled = false,
    username = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.username"),
    password = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.password"),
  )

  def getToteutus(oid: String) =
    searchItems[ToteutusIndexed](List(ElasticQueries.toteutusByOid(oid)).asJava)
  def searchToteutukset(tila: String) = 
    searchItems[ToteutusIndexed](List(ElasticQueries.toteutuksetByTila(tila)).asJava)
}

object ToteutusOps extends ToteutusOps(ElasticsearchClient.client, ElasticsearchClient.clientJava)

object ElasticClient extends Logging {

  def testConnection(): Boolean = {
    KoutaConfigurationFactory.setupWithDefaultTemplateFile()
    try {
      ElasticsearchClient.clientJava.ping().value
    } catch {
      case e: java.lang.ExceptionInInitializerError =>
        logger.warn("Got exception:", e)
        logger.warn("with cause:", e.getCause)
        false
    }
  }

  def getToteutus(oid: String): List[ToteutusIndexed] = ToteutusOps.getToteutus(oid)
  def listPublished(): List[ToteutusIndexed] = ToteutusOps.searchToteutukset("julkaistu")

}
