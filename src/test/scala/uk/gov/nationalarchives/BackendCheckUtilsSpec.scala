package uk.gov.nationalarchives

import io.circe.Printer.spaces2
import org.scalatest.flatspec.AnyFlatSpec
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, MockitoSugar, clazz}
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers._
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.AbortableInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse, PutObjectRequest, PutObjectResponse}
import uk.gov.nationalarchives.BackendCheckUtils.{Antivirus, ChecksumResult, FFID, File, FileCheckResults, Input, RedactedFilePairs, RedactedResults, Status, StatusResult}

import java.io.ByteArrayInputStream
import java.util.UUID
import scala.io.Source
class BackendCheckUtilsSpec extends AnyFlatSpec with MockitoSugar with EitherValues {

  "the case classes" should "create the expected json" in {
    val consignmentId = UUID.fromString("2f1261c5-d5e7-4865-8078-c0bc6333164f")
    val fileId = UUID.fromString("ec5ca215-c9cb-46d6-9c9e-ec8b90fed1db")
    val userId = UUID.fromString("18c24625-e336-4dca-bda8-9bea30eb213b")
    val originalFileId = UUID.fromString("db75e4e5-ee0a-4269-88d2-c1de8c73020d")
    val ffid = FFID(fileId, "software", "softwareVersion", "binarySignatureFileVersion", "containerSignatureFileVersion", "method", Nil) :: Nil
    val checksum = ChecksumResult("checksum", fileId) :: Nil
    val av = Antivirus(fileId, "software", "softwareVersion", "databaseVersion", "result", 1L) :: Nil
    val json = Input(
      List(File(consignmentId, fileId, userId, "standard", "0", "originalFilePath", "checksum", FileCheckResults(av, checksum, ffid))),
      RedactedResults(RedactedFilePairs(originalFileId, "original", fileId, "redacted") :: Nil, Nil),
      StatusResult(
        List(
          Status(UUID.fromString("27506737-37fa-4899-b494-4871f7bc3141"), "Consignment", "Status", "StatusValue"),
          Status(UUID.fromString("847e1b70-f3d6-4f4d-8f60-1f307a7df126"), "Consignment", "OverwriteStatus", "OverwriteStatusValue", overwrite = true)
        )
      )
    ).asJson.printWith(spaces2)
    val expectedJson = Source.fromResource("expected_input.json").mkString
    json.trim should equal(expectedJson.trim)
  }

  "getResultJson" should "return the correct result if the json is valid" in {
    val s3Client = mock[S3Client]
    val expectedJson = Source.fromResource("expected_input.json").mkString
    val inputStream = new ByteArrayInputStream(expectedJson.getBytes())
    val response = new ResponseInputStream(GetObjectResponse.builder().build(), AbortableInputStream.create(inputStream))
    when(s3Client.getObject(any[GetObjectRequest])).thenReturn(response)
    val result = new BackendCheckUtils(s3Client).getResultJson("key", "bucket")
    result.isRight should be(true)
    result.value should equal(decode[Input](expectedJson).value)
  }

  "getResultJson" should "return an error if the json is invalid" in {
    val s3Client = mock[S3Client]
    val inputStream = new ByteArrayInputStream("{}".getBytes())
    val response = new ResponseInputStream(GetObjectResponse.builder().build(), AbortableInputStream.create(inputStream))
    when(s3Client.getObject(any[GetObjectRequest])).thenReturn(response)
    val result = new BackendCheckUtils(s3Client).getResultJson("key", "bucket")
    result.isLeft should be(true)
  }

  "getResultJson" should "return an error if the s3 request fails" in {
    val s3Client = mock[S3Client]
    when(s3Client.getObject(any[GetObjectRequest])).thenThrow(new Exception("S3 Error"))
    val result = new BackendCheckUtils(s3Client).getResultJson("key", "bucket")
    result.isLeft should be(true)
    result.left.value.getMessage should equal("S3 Error")
  }

  "getResultJson" should "call getObject with the correct parameters" in {
    val s3Client = mock[S3Client]
    val getObjectCaptor: ArgumentCaptor[GetObjectRequest] = ArgumentCaptor.forClass(classOf[GetObjectRequest])
    val inputStream = new ByteArrayInputStream(Source.fromResource("expected_input.json").mkString.getBytes())
    val response = new ResponseInputStream(GetObjectResponse.builder().build(), AbortableInputStream.create(inputStream))
    when(s3Client.getObject(getObjectCaptor.capture())).thenReturn(response)
    new BackendCheckUtils(s3Client).getResultJson("key", "bucket")
    val requestValue = getObjectCaptor.getValue
    requestValue.key() should equal("key")
    requestValue.bucket() should equal("bucket")
  }

  "writeResultJson" should "call return the original s3 input" in {
    val s3Client = mock[S3Client]
    when(s3Client.putObject(any[PutObjectRequest], any[RequestBody])).thenReturn(PutObjectResponse.builder.build())
    val s3Input = new BackendCheckUtils(s3Client).writeResultJson("key", "bucket", "{}")
    s3Input.value.key should equal("key")
    s3Input.value.bucket should equal("bucket")
  }

  "writeResultJson" should "call putObject with the correct parameters" in {
    val s3Client = mock[S3Client]
    val requestCaptor: ArgumentCaptor[PutObjectRequest] = ArgumentCaptor.forClass(classOf[PutObjectRequest])
    val bodyCaptor: ArgumentCaptor[RequestBody] = ArgumentCaptor.forClass(classOf[RequestBody])
    when(s3Client.putObject(requestCaptor.capture(), bodyCaptor.capture()))
      .thenReturn(PutObjectResponse.builder.build())
    new BackendCheckUtils(s3Client).writeResultJson("key", "bucket", "{}")

    requestCaptor.getValue.key() should equal("key")
    requestCaptor.getValue.bucket() should equal("bucket")
    val requestBody =
      bodyCaptor.getValue.contentStreamProvider().newStream().readAllBytes().map(_.toChar).mkString
    requestBody should equal("{}")
  }

  "writeResultJson" should "return an error if there is an error from S3" in {
    val s3Client = mock[S3Client]
    when(s3Client.putObject(any[PutObjectRequest], any[RequestBody])).thenThrow(new Exception("S3 write error"))
    val s3Input = new BackendCheckUtils(s3Client).writeResultJson("key", "bucket", "{}")

    s3Input.isLeft should be(true)
    s3Input.left.value.getMessage should equal("S3 write error")
  }
}
