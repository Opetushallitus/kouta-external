package fi.oph.kouta.europass

import fi.vm.sade.valinta.dokumenttipalvelu.Dokumenttipalvelu
import fi.oph.kouta.logging.Logging
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future
import java.io.{BufferedInputStream, FileInputStream}
import java.net.URI
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.sys.exit

object EuropassPublisherApp extends Logging {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val awsRegion = EuropassConfiguration.config.getString("europass-publisher.s3.region")
  val awsBucket = EuropassConfiguration.config.getString("europass-publisher.s3.bucketname")
  val s3Key = EuropassConfiguration.config.getString("europass-publisher.s3.keyname")
  val awsEndpointOverride = EuropassConfiguration.config.getString("europass-publisher.s3.endpoint")
  lazy val dokumenttipalvelu = getDokumenttipalvelu()

  def getDokumenttipalvelu() = {
    awsEndpointOverride match {
      case url if url.startsWith("http") =>
        new Dokumenttipalvelu(awsRegion, awsBucket) {
          override def getClient() =
            S3AsyncClient.builder()
              .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("none", "none")
              ))
              .region(Region.of(awsRegion))
              .endpointOverride(new URI(url))
              .build()
          override def getPresigner() =
            S3Presigner.builder()
              .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("none", "none")
              ))
              .region(Region.of(awsRegion))
              .endpointOverride(new URI(url))
              .build()
        }
      case _ => new Dokumenttipalvelu(awsRegion, awsBucket)
    }
  }

  def main(args : Array[String]) {
    lazy val elasticUrl = EuropassConfiguration.config.getString("europass-publisher.elasticsearch.url")
    logger.info(s"Querying koulutustarjonta from $elasticUrl")
    val fileName = Publisher.koulutustarjontaAsFile()
    logger.info(s"Toteutukset dumped in $fileName; validating")
    assert(ElmValidation.validateXml(fileName))
    logger.info(s"XML in $fileName validates against loq.xsd")
    val result = dokumenttipalvelu.putObject(
      s3Key, fileName, "application/xml", new BufferedInputStream(new FileInputStream(fileName))
    ).join()
    logger.info(s"Uploaded to S3 with result $result")
    exit
  }

}
