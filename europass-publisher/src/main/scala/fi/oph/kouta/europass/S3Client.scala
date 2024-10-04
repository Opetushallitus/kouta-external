package fi.oph.kouta.europass

import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.core.async.{AsyncRequestBody, BlockingInputStreamAsyncRequestBody}
import software.amazon.awssdk.services.s3.model.{PutObjectRequest, PutObjectResponse, GetObjectAttributesRequest, GetObjectAttributesResponse}

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import java.io._

import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters._
import scala.compat.java8.FunctionConverters._
import scala.concurrent.Future

object S3Client {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val s3EndpointOverride = EuropassConfiguration.config.getString("europass-publisher.s3.endpoint.url")
  val s3Bucket = EuropassConfiguration.config.getString("europass-publisher.s3.bucket")

  lazy val client = getClient()

  def getTestClient(): S3AsyncClient =
    S3AsyncClient.builder()
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create("none", "none")))
      .region(Region.of(EuropassConfiguration.config.getString("europass-publisher.s3.region")))
      .endpointOverride(new java.net.URI(s3EndpointOverride))
      .build()

  def getClient(): S3AsyncClient =
    if (s3EndpointOverride == "no-override")
      S3AsyncClient.create()
    else
      getTestClient()

  def objectAttributes(objectName: String): Future[GetObjectAttributesResponse] =
    toScala(client.getObjectAttributes(
      asJavaConsumer[GetObjectAttributesRequest.Builder]{r => r.bucket(s3Bucket).key(objectName)}))

  def createPipe(): (InputStream, OutputStream) = {
    val input = new PipedInputStream()
    val output = new PipedOutputStream()
    output.connect(input)
    (input, output)
  }

  def s3Writer(objectName: String): (BufferedWriter, Future[PutObjectResponse]) = {
    val (inputStream, outputStream) = createPipe()
    val body = AsyncRequestBody.forBlockingInputStream(null)  // unknown length
    val responseFuture: Future[PutObjectResponse] =
      toScala(client.putObject(
        asJavaConsumer[PutObjectRequest.Builder]{r => r.bucket(s3Bucket).key(objectName)},
        body))
    val streamerFuture: Future[Long] = Future(body.writeInputStream(inputStream))
    val combinedFuture: Future[PutObjectResponse] =
      streamerFuture.zipWith(responseFuture)((l, response) => response)
    val writer = new BufferedWriter(new OutputStreamWriter(outputStream))
    (writer, combinedFuture)
  }
}
