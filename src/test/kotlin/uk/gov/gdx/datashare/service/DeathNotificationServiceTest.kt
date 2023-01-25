package uk.gov.gdx.datashare.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.gdx.datashare.config.JacksonConfiguration
import uk.gov.gdx.datashare.config.UnknownDatasetException
import uk.gov.gdx.datashare.repository.ConsumerSubscription
import uk.gov.gdx.datashare.repository.ConsumerSubscriptionRepository
import uk.gov.gdx.datashare.repository.EventData
import uk.gov.gdx.datashare.repository.EventDataRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class DeathNotificationServiceTest {
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val eventDataRepository = mockk<EventDataRepository>()
  private val levApiService = mockk<LevApiService>()
  private val objectMapper = JacksonConfiguration().objectMapper()

  private val underTest: DeathNotificationService = DeathNotificationService(
    consumerSubscriptionRepository,
    eventDataRepository,
    levApiService,
    objectMapper,
  )

  private val ONE_SECOND_OFFSET = TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)

  @Test
  fun `mapDeathNotification maps string to full DeathNotificationDetails`() {
    val input =
      "{\"firstNames\":\"Alice\",\"lastName\":\"Smith\",\"dateOfBirth\":\"1910-01-01\",\"dateOfDeath\":\"2010-12-12\",\"address\":\"666 Inform House, 6 Inform street, Informington, Informshire\",\"sex\":\"female\"}"

    val deathNotificationDetails = underTest.mapDeathNotification(input)

    assertThat(deathNotificationDetails).isEqualTo(
      DeathNotificationDetails(
        firstNames = "Alice",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1910, 1, 1),
        dateOfDeath = LocalDate.of(2010, 12, 12),
        address = "666 Inform House, 6 Inform street, Informington, Informshire",
        sex = GenderType.Female,
      ),
    )
  }

  @Test
  fun `mapDeathNotification maps string to empty DeathNotificationDetails`() {
    val input = "{}"

    val deathNotificationDetails = underTest.mapDeathNotification(input)

    assertThat(deathNotificationDetails).isEqualTo(DeathNotificationDetails())
  }

  @Test
  fun `saveDeathNotificationEvents saves full death notifications for LEV`() {
    val dataProcessorMessage = DataProcessorMessage(
      datasetId = "DEATH_LEV",
      eventTypeId = "DEATH_NOTIFICATION",
      eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
      publisher = "HMPO",
      storePayload = false,
      subscriptionId = UUID.randomUUID(),
      id = "123456789",
      details = null,
    )
    val dataDetail = DataDetail(id = dataProcessorMessage.id!!, data = null)
    val deathRecord = DeathRecord(
      deceased = Deceased(
        forenames = "Alice",
        surname = "Smith",
        dateOfBirth = LocalDate.of(1920, 1, 1),
        dateOfDeath = LocalDate.of(2010, 1, 1),
        sex = GenderType.Female,
        address = "666 Inform House, 6 Inform street, Informington, Informshire",
      ),
      id = dataDetail.id,
      date = LocalDate.now(),
    )

    every { consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId) }
      .returns(consumerSubscriptions)
    every { levApiService.findDeathById(dataDetail.id.toInt()) }.returns(listOf(deathRecord))
    every { eventDataRepository.saveAll(any<Iterable<EventData>>()) }.returns(fakeSavedEvents)

    underTest.saveDeathNotificationEvents(dataDetail, dataProcessorMessage)

    verify(exactly = 1) {
      eventDataRepository.saveAll(
        withArg<Iterable<EventData>> {
          assertThat(it).hasSize(2)
          val simpleEvent = it.find { event -> event.consumerSubscriptionId == simpleSubscription.id }
          val complexEvent = it.find { event -> event.consumerSubscriptionId == complexSubscription.id }

          assertThat(simpleEvent?.datasetId).isEqualTo(complexEvent?.datasetId)
            .isEqualTo(dataProcessorMessage.datasetId)
          assertThat(simpleEvent?.dataId).isEqualTo(complexEvent?.dataId)
            .isEqualTo(dataDetail.id)
          assertThat(simpleEvent?.eventTime).isEqualTo(complexEvent?.eventTime)
            .isEqualTo(dataProcessorMessage.eventTime)
          assertThat(simpleEvent?.whenCreated).isCloseToUtcNow(ONE_SECOND_OFFSET)
          assertThat(complexEvent?.whenCreated).isCloseToUtcNow(ONE_SECOND_OFFSET)

          assertThat(simpleEvent?.dataPayload)
            .isEqualTo(objectMapper.writeValueAsString(simpleDeathNotificationDetails))
          assertThat(complexEvent?.dataPayload)
            .isEqualTo(objectMapper.writeValueAsString(complexDeathNotificationDetails))
        },
      )
    }
  }

  @Test
  fun `saveDeathNotificationEvents saves no death details for PASS_THROUGH`() {
    val dataProcessorMessage = DataProcessorMessage(
      datasetId = "PASS_THROUGH",
      eventTypeId = "DEATH_NOTIFICATION",
      eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
      publisher = "HMPO",
      storePayload = true,
      subscriptionId = UUID.randomUUID(),
      id = "123456789",
      details = null,
    )
    val dataDetail = DataDetail(id = dataProcessorMessage.id!!, data = null)

    every { consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId) }
      .returns(consumerSubscriptions)
    every { eventDataRepository.saveAll(any<Iterable<EventData>>()) }.returns(fakeSavedEvents)

    underTest.saveDeathNotificationEvents(dataDetail, dataProcessorMessage)

    verify(exactly = 1) {
      eventDataRepository.saveAll(
        withArg<Iterable<EventData>> {
          assertThat(it).hasSize(2)
          val simpleEvent = it.find { event -> event.consumerSubscriptionId == simpleSubscription.id }
          val complexEvent = it.find { event -> event.consumerSubscriptionId == complexSubscription.id }

          assertThat(simpleEvent?.datasetId).isEqualTo(complexEvent?.datasetId)
            .isEqualTo(dataProcessorMessage.datasetId)
          assertThat(simpleEvent?.dataId).isEqualTo(complexEvent?.dataId)
            .isEqualTo(dataDetail.id)
          assertThat(simpleEvent?.eventTime).isEqualTo(complexEvent?.eventTime)
            .isEqualTo(dataProcessorMessage.eventTime)
          assertThat(simpleEvent?.whenCreated).isCloseToUtcNow(ONE_SECOND_OFFSET)
          assertThat(complexEvent?.whenCreated).isCloseToUtcNow(ONE_SECOND_OFFSET)

          assertThat(simpleEvent?.dataPayload)
            .isNull()
          assertThat(complexEvent?.dataPayload)
            .isNull()
        },
      )
    }
  }

  @Test
  fun `saveDeathNotificationEvents throws error for not valid dataset ID`() {
    val dataProcessorMessage = DataProcessorMessage(
      datasetId = "INVALID",
      eventTypeId = "DEATH_NOTIFICATION",
      eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
      publisher = "HMPO",
      storePayload = true,
      subscriptionId = UUID.randomUUID(),
      id = "123456789",
      details = null,
    )
    val dataDetail = DataDetail(id = dataProcessorMessage.id!!, data = null)

    every { consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId) }
      .returns(consumerSubscriptions)

    val exception = assertThrows<UnknownDatasetException> {
      underTest.saveDeathNotificationEvents(dataDetail, dataProcessorMessage)
    }

    assertThat(exception.message).isEqualTo("Unknown DataSet ${dataProcessorMessage.datasetId}")

    verify(exactly = 0) { eventDataRepository.saveAll(any<Iterable<EventData>>()) }
  }

  private val simpleSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    eventType = "DEATH_NOTIFICATION",
    enrichmentFields = "firstNames,lastName",
  )
  private val complexSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    eventType = "DEATH_NOTIFICATION",
    enrichmentFields = "firstNames,lastName,sex",
  )
  private val consumerSubscriptions = listOf(simpleSubscription, complexSubscription)
  private val simpleDeathNotificationDetails = DeathNotificationDetails(
    firstNames = "Alice",
    lastName = "Smith",
  )
  private val complexDeathNotificationDetails = simpleDeathNotificationDetails.copy(
    sex = GenderType.Female,
  )
  private val fakeSavedEvents = listOf(
    EventData(
      consumerSubscriptionId = UUID.randomUUID(),
      datasetId = UUID.randomUUID().toString(),
      dataId = "HMPO",
      dataPayload = null,
    ),
    EventData(
      consumerSubscriptionId = UUID.randomUUID(),
      datasetId = UUID.randomUUID().toString(),
      dataId = "HMPO",
      dataPayload = null,
    ),
  )
}
