package fi.oph.kouta.europass.test

import org.scalatra.test.scalatest.ScalatraFlatSpec
import software.amazon.awssdk.services.s3.model.{PutObjectResponse, GetObjectAttributesResponse}
import scala.concurrent.duration._
import scala.concurrent.Await
import java.nio.file.Paths

import fi.oph.kouta.europass.S3Client

class S3ClientSpec extends ScalatraFlatSpec {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  "S3Client" should "write object contents" in {
    val name = "file-" + java.util.UUID.randomUUID().toString
    val filePath = Paths.get("src", "test", "resources", "s3-test-file.xml")
    val putResult: PutObjectResponse = S3Client.sendFile(filePath, "text/plain", name)
    assert(putResult.checksumSHA256() == "blubs")
    assert(putResult.versionId() == "foo")
    val attrResult = S3Client.objectAttributes(name)
    assert(!attrResult.deleteMarker())
    assert(attrResult.versionId() == "foo")
  }
}
