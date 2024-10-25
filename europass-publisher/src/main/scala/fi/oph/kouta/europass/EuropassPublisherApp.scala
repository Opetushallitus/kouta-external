package fi.oph.kouta.europass

import fi.vm.sade.valinta.dokumenttipalvelu.Dokumenttipalvelu
import fi.vm.sade.utils.slf4j.Logging

import scala.compat.java8.FutureConverters._
import scala.concurrent.Future
import java.io.{BufferedInputStream, File, FileInputStream}
import java.net.URI
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, ProfileCredentialsProvider, StaticCredentialsProvider, SystemPropertyCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.{PutObjectRequest, PutObjectResponse}
import software.amazon.awssdk.services.s3.{S3AsyncClient, S3Client}
import software.amazon.awssdk.services.s3.presigner.S3Presigner

import scala.concurrent.duration._
import scala.concurrent.Await

object EuropassPublisherApp extends Logging {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val awsRegion = EuropassConfiguration.config.getString("europass-publisher.s3.region")
  val awsBucket = EuropassConfiguration.config.getString("europass-publisher.s3.bucketname")
  val s3Key = EuropassConfiguration.config.getString("europass-publisher.s3.keyname")
  val awsEndpointOverride = EuropassConfiguration.config.getString("europass-publisher.s3.endpoint")
  lazy val s3Client = getS3Client()

  def getS3Client() = {
    S3Client.builder()
      .endpointOverride(new URI(awsEndpointOverride))
      .region(Region.of(awsRegion))
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create("none", "none")
      ))
      .forcePathStyle(true)
      .build()
  }

  def main(args : Array[String]) {
    val future: Future[PutObjectResponse] = Publisher.publishedToteutuksetAsFile()
      .flatMap{
        fileName: String => {
          logger.info(s"Toteutukset dumped in $fileName")
          //toScala(dokumenttipalvelu.putObject(
          //  s3Key, fileName, "application/xml", new BufferedInputStream(new FileInputStream(fileName))
          //))
          val putObjectResponse = s3Client.putObject(
            PutObjectRequest
              .builder()
              .bucket(awsBucket)
              .key(s3Key)
              .contentType("application/xml")
              .build(),
              RequestBody.fromFile(new File(fileName))
          )
          Future.successful(putObjectResponse)
      }}
    val result = Await.result(future, 60.minute)
    logger.info(s"Uploaded to S3 with result $result")
  }

}
