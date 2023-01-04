package uk.gov.gdx.datashare.service

import com.amazonaws.AmazonClientException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.DeleteObjectRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import uk.gov.gdx.datashare.config.S3Config
import uk.gov.gdx.datashare.controller.EventToPublish
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

@Service
class LegacyAdaptorInbound(
  private val amazonS3: AmazonS3,
  private val s3Config: S3Config,
  private val dataReceiverApiWebClient: WebClient
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  @SchedulerLock(name = "pollS3Bucket", lockAtMostFor = "50s", lockAtLeastFor = "50s")
  fun pollS3Bucket() {
    try {
      runBlocking {
        LockAssert.assertLocked()
        log.debug("Polling S3 bucket: ${s3Config.ingressBucket}")
        amazonS3.listObjectsV2(s3Config.ingressBucket).objectSummaries
          .forEach { s3Object ->
            s3Object.key?.let {
              processBucketObject(it)
            }
          }
      }
    } catch (e: AmazonClientException) {
      log.error("Failed to connect to S3", e)
    }
  }

  suspend fun processBucketObject(objectKey: String) {

    try {
      log.debug("Retrieving object from S3: $objectKey")
      amazonS3.getObject(s3Config.ingressBucket, objectKey)
        .also {
          log.info("Found file for processing ${it.key}")
        }.objectContent
        .let {
          getCSV(it).forEach { csvRecord ->
            with(csvRecord) {
              postDataToReceiver(
                EventToPublish(
                  id = certificateId(),
                  eventType = "DEATH_NOTIFICATION",
                  eventDetails = "${lastName()},${firstName()},${dateOfBirth()},${dateOfDeath()},${gender()},\"${address()}\"" // TODO: decide whether to format etc
                )
              )
            }
          }
        }
    } catch (ex: AmazonS3Exception) {
      log.error("Failed file upload due to AmazonS3Exception", ex)
    } catch (ex: IOException) {
      log.error("Failed file upload due to IOException", ex)
    }
      .also { archiveFile(objectKey) }
  }

  @Throws(IOException::class)
  fun getCSV(inputStream: InputStream): List<CSVRecord> {
    return CSVParser.parse(inputStream, Charsets.UTF_8, CSVFormat.DEFAULT).toList()
  }

  private fun CSVRecord.certificateId() = this[0]
  private fun CSVRecord.lastName() = this[1]
  private fun CSVRecord.firstName() = this[2]
  private fun CSVRecord.dateOfBirth() = this[3]
  private fun CSVRecord.dateOfDeath() = this[4]
  private fun CSVRecord.gender() = this[5]
  private fun CSVRecord.address() = this[6]

  private fun archiveFile(fileToArchive: String) {
    val archiveFileName =
      """$fileToArchive-${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())}"""
    log.info("Attempting to archive file $fileToArchive to $archiveFileName")
    amazonS3.copyObject(
      s3Config.ingressBucket,
      fileToArchive,
      s3Config.ingressArchiveBucket,
      archiveFileName
    )
    log.info("Copied the existing file $fileToArchive to $archiveFileName")
    amazonS3.deleteObject(DeleteObjectRequest(s3Config.ingressBucket, fileToArchive))
    log.info("Deleted the existing $fileToArchive")
  }

  suspend fun postDataToReceiver(payload: EventToPublish): ResponseEntity<Void> =
    dataReceiverApiWebClient.post()
      .uri("/events")
      .bodyValue(payload)
      .retrieve()
      .awaitBodilessEntity()
}
