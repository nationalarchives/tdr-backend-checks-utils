package uk.gov.nationalarchives

import io.circe.generic.auto._
import io.circe.parser.decode
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, PutObjectRequest}
import uk.gov.nationalarchives.BackendCheckUtils.{Input, S3Input}

import java.net.URI
import java.util.UUID
import scala.util.Try

class BackendCheckUtils(s3Client: S3Client) {

  def getResultJson(key: String, bucket: String): Either[Throwable, Input] = {
    for {
      s3Response <- Try(s3Client.getObject(GetObjectRequest.builder.bucket(bucket).key(key).build)).toEither
      decoded <- decode[Input](s3Response.readAllBytes().map(_.toChar).mkString)
    } yield decoded
  }

  def writeResultJson(key: String, bucket: String, result: String): Either[Throwable, S3Input] = {
    val responseBody = RequestBody.fromString(result)
    Try(s3Client.putObject(PutObjectRequest.builder.bucket(bucket).key(key).build(), responseBody)).toEither
      .map(_ => S3Input(key, bucket))
  }
}

object BackendCheckUtils {
  private def s3Client(endpoint: String) = {
    val httpClient: SdkHttpClient = ApacheHttpClient.builder.build
    S3Client.builder
      .region(Region.EU_WEST_2)
      .endpointOverride(URI.create(endpoint))
      .httpClient(httpClient)
      .build()
  }

  def apply(endpoint: String) = new BackendCheckUtils(s3Client(endpoint))

  trait RedactedResult

  case class StatusResult(statuses: List[Status])

  case class Status(id: UUID, statusType: String, statusName: String, statusValue: String, overwrite: Boolean = false)

  case class ChecksumResult(sha256Checksum: String, fileId: UUID)

  case class FFIDMetadataInputMatches(extension: Option[String], identificationBasis: String, puid: Option[String])

  case class FFID(fileId: java.util.UUID, software: String, softwareVersion: String, binarySignatureFileVersion: String, containerSignatureFileVersion: String, method: String, matches: List[FFIDMetadataInputMatches])

  case class Antivirus(fileId: UUID, software: String, softwareVersion: String, databaseVersion: String, result: String, datetime: Long)

  case class FileCheckResults(antivirus: List[Antivirus], checksum: List[ChecksumResult], fileFormat: List[FFID])

  case class File(
                   consignmentId: UUID,
                   fileId: UUID,
                   userId: UUID,
                   consignmentType: String,
                   fileSize: String,
                   clientChecksum: String,
                   originalPath: String,
                   fileCheckResults: FileCheckResults
                 )

  case class RedactedResults(redactedFiles: List[RedactedFilePairs], errors: List[RedactedErrors])

  case class RedactedErrors(fileId: UUID, cause: String) extends RedactedResult

  case class RedactedFilePairs(originalFileId: UUID, originalFilePath: String, redactedFileId: UUID, redactedFilePath: String) extends RedactedResult

  case class Input(results: List[File], redactedResults: RedactedResults, statuses: StatusResult)

  case class S3Input(key: String, bucket: String)
}

