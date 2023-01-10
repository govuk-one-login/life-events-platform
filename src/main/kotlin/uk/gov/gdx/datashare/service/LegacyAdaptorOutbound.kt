package uk.gov.gdx.datashare.service

import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.S3Config
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


@Service
class LegacyAdaptorOutbound (
  private val amazonS3: AmazonS3,
  private val s3Config: S3Config,
  private val objectMapper: ObjectMapper,
  private val egressEventDataRepository: EgressEventDataRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
  @SchedulerLock(name = "publishToS3Bucket", lockAtMostFor = "50s", lockAtLeastFor = "50s")
  fun publishToS3Bucket() {
    try {
      runBlocking {
        LockAssert.assertLocked()
        LegacyAdaptorInbound.log.debug("Looking for events to publish to S3 bucket: ${s3Config.egressBucket}")


        // find outbound events
        egressEventDataRepository.findAllByConsumerName("Internal Adaptor")
          .filter { it.dataPayload != null }
          .toList()
          .groupBy { it.consumerSubscriptionId }
          .map { eventMap ->

            // use first entry to build the header
            val events = eventMap.value

            val csvSchemaBuilder = CsvSchema.builder()
            val firstObject = objectMapper.readValue(events[0].dataPayload, JsonNode::class.java)
            firstObject.fieldNames().forEachRemaining { fieldName ->
              csvSchemaBuilder.addColumn(fieldName)
            }
            val csvSchema = csvSchemaBuilder.build().withHeader()

            val fileName = """${eventMap.key}-${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())}.csv"""
            val csvFile = File(fileName)

            val jsonTree = objectMapper.readTree(objectMapper.writeValueAsString(
              events.map {
               objectMapper.readValue(it.dataPayload, JsonNode::class.java)
              }.toList()))

            CsvMapper().writerFor(JsonNode::class.java)
              .with(csvSchema)
              .writeValue(csvFile, jsonTree)

            amazonS3.putObject(s3Config.egressBucket, fileName, csvFile)

            events.forEach { event -> egressEventDataRepository.deleteById(event.id) }
          }
      }
    } catch (e: Exception) {
      log.error("Exception", e)
    }
  }



}

