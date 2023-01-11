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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.S3Config
import uk.gov.gdx.datashare.repository.EgressEventData
import uk.gov.gdx.datashare.repository.EgressEventDataRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

  @ConditionalOnProperty(name = ["consume-outbound-by-queue"], havingValue = "false")
  @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
  @SchedulerLock(name = "publishToS3Bucket", lockAtMostFor = "50s", lockAtLeastFor = "50s")
  fun publishToS3Bucket() {
    try {
      runBlocking {
        LockAssert.assertLocked()
        log.debug("Looking for events to publish to S3 bucket: ${s3Config.egressBucket}")

        // find outbound events
        egressEventDataRepository.findAllByConsumerName("Internal Adaptor")
          .filter { it.dataPayload != null }
          .toList()
          .groupBy { it.consumerSubscriptionId }
          .map { eventMap ->

            val events = eventMap.value
            val csvData = CsvMapper().writerFor(JsonNode::class.java)
              .with(buildCsvSchema(events[0].dataPayload!!))
              .writeValueAsString(buildJsonTree(events))

            log.debug("Pushed event subscription ${eventMap.key} to ${s3Config.egressBucket}")
            val fileName = """${eventMap.key}-${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())}.csv"""

            amazonS3.putObject(s3Config.egressBucket, fileName, csvData)

            events.forEach { event -> egressEventDataRepository.deleteById(event.id) }
          }
      }
    } catch (e: Exception) {
      log.error("Exception", e)
    }
  }

  private fun buildJsonTree(events: List<EgressEventData>): JsonNode? =
    objectMapper.readTree(objectMapper.writeValueAsString(
      events.map {
        objectMapper.readValue(it.dataPayload, JsonNode::class.java)
      }.toList()
    )
    )

  private fun buildCsvSchema(firstEvent: String): CsvSchema? {
    val csvSchemaBuilder = CsvSchema.builder()
    val firstObject = objectMapper.readValue(firstEvent, JsonNode::class.java)
    firstObject.fieldNames().forEachRemaining { fieldName ->
      csvSchemaBuilder.addColumn(fieldName)
    }
    return csvSchemaBuilder.build().withHeader()
  }


}

