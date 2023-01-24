package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.UnknownDatasetException
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDate

@Service
class DeathNotificationService(
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val eventDataRepository: EventDataRepository,
  private val levApiService: LevApiService,
  private val objectMapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun mapDeathNotification(dataPayload: String): DeathNotificationDetails? =
    objectMapper.readValue(dataPayload, DeathNotificationDetails::class.java)

  fun saveDeathNotificationEvents(
    details: DataDetail,
    dataProcessorMessage: DataProcessorMessage,
  ) {
    val consumerSubscriptions = consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId)

    val fullyEnrichedData = getAllEnrichedData(
      dataProcessorMessage.datasetId,
      details.id,
    )

    val eventData = consumerSubscriptions.map {
      val dataPayload =
        enrichEventPayload(
          it.enrichmentFields.split(",").toList(),
          fullyEnrichedData,
        )

      EventData(
        consumerSubscriptionId = it.id,
        datasetId = dataProcessorMessage.datasetId,
        dataId = details.id,
        dataPayload = dataPayload?.let { objectMapper.writeValueAsString(dataPayload) },
        eventTime = dataProcessorMessage.eventTime,
      )
    }.toList()

    eventDataRepository.saveAll(eventData).toList()
  }

  private fun enrichEventPayload(
    enrichmentFields: List<String>,
    fullyEnrichedData: DeathNotificationDetails?,
  ): DeathNotificationDetails? {
    return EnrichmentService.getDataWithOnlyFields(
      objectMapper,
      fullyEnrichedData,
      enrichmentFields,
    )
  }

  private fun getAllEnrichedData(
    datasetId: String,
    dataId: String,
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

    "PASS_THROUGH" -> {
      null
    }

    else -> {
      throw UnknownDatasetException("Unknown DataSet $datasetId")
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
