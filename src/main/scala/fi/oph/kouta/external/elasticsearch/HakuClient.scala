package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{must, should, termsQuery}
import com.sksamuel.elastic4s.ElasticClient

import java.time.Instant
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.domain.Tallennettu
import fi.oph.kouta.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.indexed.HakuIndexed
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.oph.kouta.util.TimeUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuClient(val client: ElasticClient) extends ElasticsearchClient with KoutaJsonFormats {
  val index: String = "haku-kouta"

  def getHaku(oid: HakuOid): Future[(Haku, Instant)] =
    getItem(oid.s)
      .map(debugJson)
      .map(_.to[HakuIndexed])
      .map(_.toHaku())
      .map(h => (h, TimeUtils.localDateTimeToInstant(h.modified.get.value)))

  def findByOids(hakuOids: Set[HakuOid]): Future[Seq[Haku]] = {
    val hakukohteetQuery = should(termsQuery("oid", hakuOids.map(_.toString)))
    searchItems[HakuIndexed](Some(must(hakukohteetQuery))).map(_.map(_.toHaku()))
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
    val ataruIdQuery      = ataruId.map(termsQuery("hakulomakeAtaruId.keyword", _))
    val alkamisvuosiQuery = vuosi.map(termsQuery("metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi", _))
    val hakuvuosiQuery    = vuosi.map(termsQuery("hakuvuosi", _))
    val tarjoajaQuery = tarjoajaOids.map(oids =>
      should(
        oids.map(oid =>
          should(
            termsQuery("hakukohteet.jarjestyspaikka.oid", oid.toString),
            termsQuery("hakukohteet.toteutus.tarjoajat.oid", oid.toString)
          )
        )
      )
    )
    val query = ataruIdQuery ++ tarjoajaQuery ++ alkamisvuosiQuery ++ hakuvuosiQuery
    searchItems[HakuIndexed](if (query.isEmpty) None else Some(must(query)))
      .map(_.filter(byTarjoajaAndTila(tarjoajaOids, _)).map(_.toHaku()))
  }
}

object HakuClient extends HakuClient(ElasticsearchClient.client)
