package fi.oph.kouta.europass

import org.json4s._
import org.json4s.jackson.JsonMethods.parse
import org.asynchttpclient.Dsl._
import org.asynchttpclient._
import com.github.tototoshi.csv.CSVReader

import fi.oph.kouta.logging.Logging
import fi.oph.kouta.external.util.KoutaJsonFormats

import java.io.{File, BufferedWriter, FileWriter}
import scala.sys.process.Process

class OrganisationClient extends Logging with KoutaJsonFormats {

  lazy val orgUrl = EuropassConfiguration.config.getString("europass-publisher.organisation.url")
  lazy val roleArn = EuropassConfiguration.config.getString("europass-publisher.lampi-s3.role-arn")
  lazy val externalId = EuropassConfiguration.config.getString("europass-publisher.lampi-s3.external-id")
  lazy val csvBucket = EuropassConfiguration.config.getString("europass-publisher.lampi-s3.org-data-bucket")

  val httpClient = asyncHttpClient()

  def awsConfig(): String = {
    s"""[profile lampi-crossorganisation]
      region = eu-west-1
      role_session_name = lampi-read-europass
      credential_source = EcsContainer
      role_arn = $roleArn
      external_id = $externalId
    """
  }

  def writeAwsConfig(): String = {
    val tempFile = File.createTempFile("aws-config", ".ini")
    logger.info(s"writeAwsConfig: using filename $tempFile")
    tempFile.deleteOnExit()
    val writer = new BufferedWriter(new FileWriter(tempFile))
    writer.write(awsConfig())
    writer.close()
    tempFile.getPath()
  }

  def getOrganisationCsv(): String = {
    val configFile = writeAwsConfig()
    val tempFile = File.createTempFile("organisations", ".csv")
    tempFile.deleteOnExit()
    val tempFileName = tempFile.getPath()
    val awsCommand = List(
      "aws",
      "--profile",
      "lampi-crossorganisation",
      "s3",
      "cp",
      s"${csvBucket}osoite.csv",
      tempFileName
    )
    Process(awsCommand, new java.io.File("/tmp"), "AWS_CONFIG_FILE" -> configFile).!
    tempFileName
  }

  def getOrganisationInfo(): Map[(String, String, String), Seq[String]] = {
    val orgFile = getOrganisationCsv()
    val reader = CSVReader.open(new File(orgFile))
    reader.iterator.flatMap{orgLine =>
      // CSV fields: organisaatio_oid,osoitetyyppi,osoite,postinumero,postitoimipaikka,kieli
      // oid, osoitetyyppi, kieli
      try {
        Some(((orgLine(0), orgLine(1), orgLine(5).split("_")(1).split("#")(0)), orgLine))
      } catch {
        case e: ArrayIndexOutOfBoundsException => None
        case e: IndexOutOfBoundsException => None
      }
    }.toMap
  }

  lazy val organisations = getOrganisationInfo()

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
