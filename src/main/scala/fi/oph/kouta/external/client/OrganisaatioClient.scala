package fi.oph.kouta.external.client

import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.domain.oid.OrganisaatioOid
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.properties.OphProperties
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.annotation.tailrec

object OrganisaatioClient extends HttpClient with KoutaJsonFormats {
  val urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties

  case class OrganisaatioResponse(numHits: Int, organisaatiot: List[OidAndChildren])
  case class OidAndChildren(oid: OrganisaatioOid, children: List[OidAndChildren], parentOidPath: String)

  def getAllChildOidsFlat(oid: OrganisaatioOid): Seq[OrganisaatioOid] = getHierarkia(oid, children(oid, _))

  private def getHierarkia[R](oid: OrganisaatioOid, result: List[OidAndChildren] => R) = {
    val url = urlProperties.url("organisaatio-service.organisaatio.hierarkia", queryParams(oid.toString))
    get(url) { response =>
      result(parse(response).extract[OrganisaatioResponse].organisaatiot)
    }
  }

  private def queryParams(oid: String) =
    toQueryParams("oid" -> oid, "aktiiviset" -> "true", "suunnitellut" -> "true", "lakkautetut" -> "false")

  private def children(oid: OrganisaatioOid, organisaatiot: List[OidAndChildren]): Seq[OrganisaatioOid] =
    find(oid, organisaatiot).map(x => x.oid +: childOidsFlat(x)).getOrElse(Seq()).distinct

  private def parentsAndChildren(oid: OrganisaatioOid, organisaatiot: List[OidAndChildren]): Seq[OrganisaatioOid] =
    find(oid, organisaatiot).map(x => parentOidsFlat(x) ++ Seq(x.oid) ++ childOidsFlat(x)).getOrElse(Seq()).distinct

  @tailrec
  private def find(oid: OrganisaatioOid, level: List[OidAndChildren]): Option[OidAndChildren] =
    level.find(_.oid == oid) match {
      case None if level.isEmpty => None
      case Some(c)               => Some(c)
      case None                  => find(oid, level.flatMap(_.children))
    }

  private def childOidsFlat(item: OidAndChildren): Seq[OrganisaatioOid] =
    item.children.flatMap(c => c.oid +: childOidsFlat(c))

  private def parentOidsFlat(item: OidAndChildren): Seq[OrganisaatioOid] =
    item.parentOidPath.split('/').toSeq.reverse.map(OrganisaatioOid)

}