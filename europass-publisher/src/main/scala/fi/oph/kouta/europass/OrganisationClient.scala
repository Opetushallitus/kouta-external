package fi.oph.kouta.europass

import org.json4s._
import org.json4s.jackson.JsonMethods.parse
import org.asynchttpclient.Dsl._
import org.asynchttpclient._

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.util.KoutaJsonFormats

class OrganisationClient extends Logging with KoutaJsonFormats {

  lazy val orgUrl = EuropassConfiguration.config.getString("europass-publisher.organisation.url")
  val httpClient = asyncHttpClient()

  def getOrganisation(oid: String): JValue = {
    val req = get(s"${orgUrl}/${oid}")
    val resp: Response = httpClient.executeRequest(req).toCompletableFuture().join()
    resp match {
      case r if r.getStatusCode == 200 => parse(r.getResponseBodyAsStream())
      case r => throw new RuntimeException(s"Organisation query for oid $oid failed: ${r.getResponseBody()}")
    }
  }

  def organisationAddress(org: JValue): Option[String] = {
    try {
      val yhteystiedot: Seq[JValue] = (org \ "yhteystiedot").children
      val kayntiosoite: JValue = yhteystiedot.filter(yt => (yt \ "osoiteTyyppi") == JString("kaynti"))(0)
      val osoite = (kayntiosoite \ "osoite").extract[String]
      val postinro = (kayntiosoite \ "postinumeroUri").extract[String].split("_")(1)
      val kunta = (kayntiosoite \ "postitoimipaikka").extract[String]
      Some(s"$osoite, $postinro  $kunta")
    } catch {
      case e: IndexOutOfBoundsException =>
        val oid = org \ "oid"
        logger.warn(s"No address found for organisation: $oid")
        None
    }
  }

  def getOrganisationAddress(oid: String): Option[String] =
    organisationAddress(getOrganisation(oid))
}

object OrganisationClient extends OrganisationClient
