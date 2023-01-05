package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.config.AuthenticationFacade
import uk.gov.gdx.datashare.controller.EventInformation
import uk.gov.gdx.datashare.repository.*
import java.time.LocalDate
import java.util.UUID

@Service
class EventDataRetrievalService(
  private val egressEventDataRepository: EgressEventDataRepository,
  private val auditService: AuditService,
  private val authenticationFacade: AuthenticationFacade,
  private val levApiService: LevApiService,
  private val hmrcApiService: HmrcApiService,
  private val consumerSubscriptionRepository: ConsumerSubscriptionRepository,
  private val consumerRepository: ConsumerRepository
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun retrieveData(eventId: UUID): EventInformation {

    // Retrieve the ID from the lookup table
    val event = egressEventDataRepository.findById(eventId) ?: throw RuntimeException("Event $eventId Not Found")

    val oauthClient = authenticationFacade.getUsername()
    log.info("Looking up event Id {} for client request {}", eventId, oauthClient)

    val consumerSubscription = consumerSubscriptionRepository.findById(event.consumerSubscriptionId)
      ?: throw RuntimeException("Consumer subscription ${event.consumerSubscriptionId} not found")

    // check if client is allowed to send
    if (consumerSubscription.callbackClientId != oauthClient) {
      throw RuntimeException("Client $oauthClient is not allowed to consume ${consumerSubscription.ingressEventType} events")
    }

    auditService.sendMessage(
      auditType = AuditType.CLIENT_CONSUMED_EVENT,
      id = eventId.toString(),
      details = consumerSubscription.ingressEventType,
      username = consumerRepository.findById(consumerSubscription.consumerId)?.name
        ?: throw RuntimeException("Consumer not found for ID ${consumerSubscription.consumerId}")
    )

    return when (event.datasetId) {
      "DEATH_LEV" -> {
        // get the data from the LEV
        val citizenDeathId = event.dataId.toInt()
        levApiService.findDeathById(citizenDeathId)
          .map {
            val nino = if (consumerSubscription.ninoRequired) getNino(
              it.deceased.surname,
              it.deceased.forenames,
              it.deceased.dateOfDeath
            ) else null

            EventInformation(
              eventType = consumerSubscription.ingressEventType,
              eventId = eventId,
              details = DeathNotification(
                deathDetails = DeathDetails(
                  forenames = it.deceased.forenames,
                  surname = it.deceased.surname,
                  dateOfBirth = it.deceased.dateOfBirth,
                  dateOfDeath = it.deceased.dateOfDeath,
                  sex = it.deceased.sex,
                  address = it.deceased.address
                ),
                additionalInformation = if (nino != null) AdditionalInformation(
                  nino = nino
                ) else null
              )
            )
          }.first()
      }

      "DEATH_CSV" -> {
        // get the data from the data store - it's a CSV file
        val csvLine = event.dataPayload!!.split(",").toTypedArray()
        val (surname, forenames, dateOfBirth, dateOfDeath, sex) = csvLine

        val nino =
          if (consumerSubscription.ninoRequired) getNino(surname, forenames, LocalDate.parse(dateOfBirth)) else null

        EventInformation(
          eventType = consumerSubscription.ingressEventType,
          eventId = eventId,
          details = DeathNotification(
            deathDetails = DeathDetails(
              forenames = forenames,
              surname = surname,
              dateOfBirth = LocalDate.parse(dateOfBirth),
              dateOfDeath = LocalDate.parse(dateOfDeath),
              sex = sex
            ),
            additionalInformation = if (nino != null) AdditionalInformation(
              nino = nino
            ) else null
          )
        )
      }

      "PASS_THROUGH" -> {
        EventInformation(
          eventType = consumerSubscription.ingressEventType,
          eventId = eventId,
          details = event.dataPayload,
        )
      }

      else -> {
        throw RuntimeException("Unknown DataSet ${event.datasetId}")
      }
    }
  }

  suspend fun getNino(
    surname: String,
    forenames: String,
    dateOfBirth: LocalDate
  ): String? {
    log.debug("Looking up NINO from HMRC : search by {}, {}, {}", surname, forenames, dateOfBirth)
    return hmrcApiService.getNiNo(
      surname = surname,
      firstname = forenames,
      dob = dateOfBirth
    ).ni_number
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Death Notification Details", required = true)
data class DeathNotification(
  @Schema(description = "Core details about the event", required = true)
  val deathDetails: DeathDetails,
  @Schema(description = "Additional information that can be enriched with this event", required = false)
  val additionalInformation: AdditionalInformation? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Core details about the death", required = true)
data class DeathDetails(
  @Schema(description = "Forenames of the deceased", required = true)
  val forenames: String,
  @Schema(description = "Surname of the deceased", required = true)
  val surname: String,
  @Schema(description = "Date of birth of the deceased", required = true)
  val dateOfBirth: LocalDate,
  @Schema(description = "Date of death of the deceased", required = true)
  val dateOfDeath: LocalDate,
  @Schema(description = "Address (if provided) of the deceased", required = true)
  val sex: String,
  @Schema(description = "Core details about the event", required = false)
  val address: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Additional details about the event (enriched data)", required = false)
data class AdditionalInformation(
  @Schema(description = "National Insurance Number", required = false)
  val nino: String? = null,
)
