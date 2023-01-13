package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.*
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDate
import java.util.*

@Service
class DeathNotificationService(
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val egressEventDataRepository: EgressEventDataRepository,
  private val eventPublishingService: EventPublishingService,
  private val levApiService: LevApiService,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun mapDeathNotification(dataPayload: String): DeathNotificationDetails? =
    objectMapper.readValue(dataPayload, DeathNotificationDetails::class.java)

  suspend fun saveDeathNotificationEvents(
    eventData: IngressEventData,
    details: DataDetail,
    dataProcessorMessage: DataProcessorMessage,
  ) {
    val consumerSubscriptions = consumerSubscriptionRepository.findAllByIngressEventType(eventData.eventTypeId)

    val fullyEnrichedData = getAllEnrichedData(
      dataProcessorMessage.datasetId,
      details.id,
      details.data as String?,
    )
    log.debug("Data enriched with details $fullyEnrichedData")

    val egressEventData = consumerSubscriptions.map {
      val dataPayload =
        enrichEventPayload(
          it.enrichmentFields.split(",").toList(),
          fullyEnrichedData,
        )

      EgressEventData(
        consumerSubscriptionId = it.id,
        ingressEventId = eventData.eventId,
        datasetId = dataProcessorMessage.datasetId,
        dataId = details.id,
        dataPayload = dataPayload?.let { objectMapper.writeValueAsString(dataPayload) },
        eventTime = dataProcessorMessage.eventTime,
      )
    }.toList()

    val savedEgressEvents = egressEventDataRepository.saveAll(egressEventData).toList()

    savedEgressEvents.forEach {
      eventPublishingService.storeAndPublishEvent(it)
    }
  }

  private suspend fun enrichEventPayload(
    enrichmentFields: List<String>,
    fullyEnrichedData: DeathNotificationDetails?,
  ): DeathNotificationDetails? {
    return EnrichmentService.getDataWithOnlyFields(
      objectMapper,
      fullyEnrichedData,
      enrichmentFields,
    )
  }

  private suspend fun getAllEnrichedData(
    datasetId: String,
    dataId: String,
    dataPayload: String?,
  ): DeathNotificationDetails? = when (datasetId) {
    "DEATH_LEV" -> {
      // get the data from the LEV
      val citizenDeathId = dataId.toInt()
      levApiService.findDeathById(citizenDeathId)
        .map {
          DeathNotificationDetails(
            firstName = it.deceased?.forenames,
            lastName = it.deceased?.surname,
            dateOfBirth = it.deceased?.dateOfBirth,
            dateOfDeath = it.deceased?.dateOfDeath,
            sex = it.deceased?.sex,
            address = it.deceased?.address,
          )
        }.first()
    }

    "DEATH_CSV" -> {
      // get the data from the data store - it's a CSV file
      val csvLine = dataPayload!!.split(",").toTypedArray()
      DeathNotificationDetails(
        firstName = csvLine[1],
        lastName = csvLine[0],
        dateOfBirth = LocalDate.parse(csvLine[2]),
        dateOfDeath = LocalDate.parse(csvLine[3]),
        sex = csvLine[4],
        address = if (csvLine.count() > 5) {
          csvLine[5]
        } else {
          null
        },
      )
    }

    "PASS_THROUGH" -> {
      null
    }

    else -> {
      throw RuntimeException("Unknown DataSet $datasetId")
    }
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Death notification")
data class DeathNotificationDetails(
  @Schema(description = "First name", required = false, example = "Bob")
  val firstName: String? = null,
  @Schema(description = "Last name", required = false, example = "Smith")
  val lastName: String? = null,
  @Schema(description = "Date of Birth", required = false, example = "2001-12-31T12:34:56")
  val dateOfBirth: LocalDate? = null,
  @Schema(description = "Date of Death", required = false, example = "2021-12-31T12:34:56")
  val dateOfDeath: LocalDate? = null,
  @Schema(description = "Address", required = false, example = "888 Death House, 8 Death lane, Deadington, Deadshire")
  val address: String? = null,
  @Schema(description = "Sex", required = false, example = "Male")
  val sex: String? = null,
)
