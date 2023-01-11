package uk.gov.gdx.datashare.service

import com.amazonaws.services.s3.AmazonS3
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.gdx.datashare.config.S3Config
import uk.gov.gdx.datashare.repository.EgressEventData
import java.time.OffsetDateTime
import java.util.*

@Service
class SubscriptionBasedLegacyAdaptorOutbound(
  private val amazonS3: AmazonS3,
  private val s3Config: S3Config,
  private val objectMapper: ObjectMapper,
  private val eventDataRetrievalApiWebClient: WebClient,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @ConditionalOnProperty(name = ["consume-outbound-by-queue"], havingValue = "true")
  @JmsListener(destination = "adaptor", containerFactory = "awsQueueContainerFactoryProxy")
  fun onPublishedEvent(eventMessage: String) = runBlocking {
    val (message, messageAttributes) = objectMapper.readValue(eventMessage, EventTopicMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    val event = objectMapper.readValue(message, EventMessage::class.java)
    processEvent(event.id)
  }

  private suspend fun processEvent(
      id: UUID
  ) {
    // Go and get data from Event Retrieval API
    val lifeEvent = getEventPayload(id)
    log.debug("Obtained event: ${lifeEvent.eventId}")

    val json = objectMapper.writeValueAsString(lifeEvent.eventData)
    val csvData = CsvMapper().writerFor(LinkedHashMap::class.java)
      .with(buildCsvSchema(json))
      .writeValueAsString(lifeEvent.eventData)

    val filename = "${lifeEvent.eventId}.csv"
    log.debug("Writing $filename to S3 bucket ${s3Config.egressBucket}")
    amazonS3.putObject(s3Config.egressBucket, filename, csvData)
    removeEvent(lifeEvent.eventId)
  }

  private fun buildCsvSchema(json: String): CsvSchema? {
    val csvSchemaBuilder = CsvSchema.builder()
    val firstObject = objectMapper.readValue(json, JsonNode::class.java)
    firstObject.fieldNames().forEachRemaining { fieldName ->
      csvSchemaBuilder.addColumn(fieldName)
    }
    return csvSchemaBuilder.build().withHeader()
  }


  suspend fun getEventPayload(id: UUID): EventNotification =
    eventDataRetrievalApiWebClient.get()
      .uri("/events/$id")
      .retrieve()
      .awaitBody()

  suspend fun removeEvent(id: UUID) =
    eventDataRetrievalApiWebClient.delete()
      .uri("/events/$id")
      .retrieve()
      .awaitBodilessEntity()
}

data class AttributeType(val Value: String, val Type: String)
data class MessageAttributes(val eventType: AttributeType, val consumer: AttributeType)
data class EventTopicMessage(
  val Message: String,
  val MessageAttributes: MessageAttributes
)

data class EventMessage(
  val id: UUID,
  val occurredAt: OffsetDateTime,
  val description: String
)
