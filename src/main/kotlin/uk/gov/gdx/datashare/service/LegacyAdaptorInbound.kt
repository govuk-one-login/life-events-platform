package uk.gov.gdx.datashare.service

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import uk.gov.gdx.datashare.resource.EventToPublish
import uk.gov.gdx.datashare.resource.EventType
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class LegacyAdaptorInbound(
  @Value("\${api.base.s3.ingress}") private val ingressBucket: String,
  @Value("\${api.base.s3.ingress-archive}") private val archiveIngressBucket: String,
  private val dataReceiverApiWebClient: WebClient
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  fun pollFtpServer() {
    val testFtpClient = FTPClient()
    val host = "localhost"
    val port = 31000
    try {
      testFtpClient.connect(host, port)
      testFtpClient.login("user", "password")

      listFiles(ftpClient = testFtpClient).forEach {
        log.debug("Retrieving file ${it.first}")
        val fileHandle = FileOutputStream(it.first)
        testFtpClient.retrieveFile(it.first, fileHandle)
        val file = File(it.first)
        file.forEachLine {
          runBlocking {
            postDataToReceiver(EventToPublish(eventType = EventType.DEATH_NOTIFICATION, eventDetails = it))
          }
        }
        val newLocation = "/archive/${it.first}"
        testFtpClient.rename(it.first, newLocation)
        file.delete()
      }

      testFtpClient.logout()
    } catch (e: ConnectException) {
      log.warn("Failed to connect to FTP server")
    } catch (e: RuntimeException) {
      log.error("Failed process line", e)
    }
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  fun pollS3Bucket() {
    try {
      runBlocking {
        S3Client {
          region = "eu-west-2"
        }.use { s3 ->
          log.debug("Polling S3 bucket: $ingressBucket")
          val listObjectsRequest = ListObjectsRequest {
            bucket = ingressBucket
          }
          val objects = s3.listObjects(listObjectsRequest).contents

          objects?.forEach { s3Object ->
            s3Object.key?.let {
              processBucketObject(s3, it)
            }
          }
        }
      }
    } catch (e: Exception) {
      log.error("Failed to connect to S3", e)
    }
  }

  suspend fun postDataToReceiver(payload: EventToPublish): ResponseEntity<Void> =
    dataReceiverApiWebClient.post()
      .uri("/event-data-receiver")
      .bodyValue(payload)
      .retrieve()
      .awaitBodilessEntity()

  suspend fun processBucketObject(s3: S3Client, objectKey: String) {
    val getObjectRequest = GetObjectRequest {
      key = objectKey
      bucket = ingressBucket
    }
    val encodedCopySource = withContext(Dispatchers.IO) {
      URLEncoder.encode("$ingressBucket/$objectKey", StandardCharsets.UTF_8.toString())
    }
    val copyObjectRequest = CopyObjectRequest {
      copySource = encodedCopySource
      key = objectKey
      bucket = archiveIngressBucket
    }
    val deleteObjectRequest = DeleteObjectRequest {
      key = objectKey
      bucket = ingressBucket
    }

    val file = File(objectKey)
    log.debug("Retrieving object from S3: $objectKey")
    s3.getObject(getObjectRequest) { response ->
      response.body?.writeToFile(file)
      file.forEachLine {
        runBlocking {
          postDataToReceiver(EventToPublish(eventType = EventType.DEATH_NOTIFICATION, eventDetails = it))
        }
      }

      log.debug("Copying object to archive: $objectKey")
      s3.copyObject(copyObjectRequest)

      log.debug("Deleting object from S3: $objectKey")
      s3.deleteObject(deleteObjectRequest)
    }
    file.delete()
  }
}

private fun listFiles(ftpClient: FTPClient, path: String? = null): List<Pair<String, LocalDateTime>> {
  return ftpClient.listFiles(path)
    .map { fp -> Pair(fp.name, LocalDateTime.ofInstant(fp.timestamp.toInstant(), fp.timestamp.timeZone.toZoneId())) }
    .sortedBy { it.second }
    .toList()
}