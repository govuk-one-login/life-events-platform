package uk.gov.gdx.datashare.uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.gdx.datashare.config.UnknownDatasetException
import uk.gov.gdx.datashare.repository.*
import uk.gov.gdx.datashare.service.*
import java.time.LocalDateTime
import java.util.*

class DataProcessorTest {
  private val objectMapper = mockk<ObjectMapper>()
  private val consumerSubscriptionRepository = mockk<ConsumerSubscriptionRepository>()
  private val eventDataRepository = mockk<EventDataRepository>()

  private val underTest: DataProcessor = DataProcessor(
    objectMapper, consumerSubscriptionRepository, eventDataRepository,
  )

  @Test
  fun `onGovEvent saves death notifications for LEV`() {
    runBlocking {
      val dataProcessorMessage = DataProcessorMessage(
        datasetId = "DEATH_LEV",
        eventTypeId = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
        publisher = "HMPO",
        subscriptionId = UUID.randomUUID(),
        id = "123456789",
      )

      coEvery { consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId) }
        .returns(consumerSubscriptions)
      coEvery { eventDataRepository.saveAll(any<Iterable<EventData>>()) }.returns(fakeSavedEvents)
      coEvery { objectMapper.readValue(any<String>(), DataProcessorMessage::class.java) }.returns(dataProcessorMessage)

      underTest.onGovEvent("string")

      coVerify(exactly = 1) {
        eventDataRepository.saveAll(
          withArg<Iterable<EventData>> {
            assertThat(it).hasSize(2)
            val simpleEvent = it.find { event -> event.consumerSubscriptionId == simpleSubscription.id }
            val complexEvent = it.find { event -> event.consumerSubscriptionId == complexSubscription.id }

            assertThat(simpleEvent?.datasetId).isEqualTo(complexEvent?.datasetId)
              .isEqualTo(dataProcessorMessage.datasetId)
            assertThat(simpleEvent?.dataId).isEqualTo(complexEvent?.dataId)
              .isEqualTo(dataProcessorMessage.id)
            assertThat(simpleEvent?.eventTime).isEqualTo(complexEvent?.eventTime)
              .isEqualTo(dataProcessorMessage.eventTime)
            assertThat(simpleEvent?.whenCreated).isEqualTo(complexEvent?.whenCreated)
              .isNull()
          },
        )
      }
    }
  }

  @Test
  fun `onGovEvent saves death notifications for PASS_THROUGH`() {
    runBlocking {
      val dataProcessorMessage = DataProcessorMessage(
        datasetId = "PASS_THROUGH",
        eventTypeId = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
        publisher = "HMPO",
        subscriptionId = UUID.randomUUID(),
        id = "123456789",
      )

      coEvery { consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId) }
        .returns(consumerSubscriptions)
      coEvery { eventDataRepository.saveAll(any<Iterable<EventData>>()) }.returns(fakeSavedEvents)
      coEvery { objectMapper.readValue(any<String>(), DataProcessorMessage::class.java) }.returns(dataProcessorMessage)

      underTest.onGovEvent("string")

      coVerify(exactly = 1) {
        eventDataRepository.saveAll(
          withArg<Iterable<EventData>> {
            assertThat(it).hasSize(2)
            val simpleEvent = it.find { event -> event.consumerSubscriptionId == simpleSubscription.id }
            val complexEvent = it.find { event -> event.consumerSubscriptionId == complexSubscription.id }

            assertThat(simpleEvent?.datasetId).isEqualTo(complexEvent?.datasetId)
              .isEqualTo(dataProcessorMessage.datasetId)
            assertThat(simpleEvent?.dataId).isEqualTo(complexEvent?.dataId)
              .isEqualTo(dataProcessorMessage.id)
            assertThat(simpleEvent?.eventTime).isEqualTo(complexEvent?.eventTime)
              .isEqualTo(dataProcessorMessage.eventTime)
            assertThat(simpleEvent?.whenCreated).isEqualTo(complexEvent?.whenCreated)
              .isNull()
          },
        )
      }
    }
  }

  @Test
  fun `saveDeathNotificationEvents throws error for not valid dataset ID`() {
    runBlocking {
      val dataProcessorMessage = DataProcessorMessage(
        datasetId = "INVALID",
        eventTypeId = "DEATH_NOTIFICATION",
        eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
        publisher = "HMPO",
        subscriptionId = UUID.randomUUID(),
        id = "123456789",
      )

      coEvery { consumerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventTypeId) }
        .returns(consumerSubscriptions)
      coEvery { objectMapper.readValue(any<String>(), DataProcessorMessage::class.java) }.returns(dataProcessorMessage)

      val exception = assertThrows<UnknownDatasetException> {
        underTest.onGovEvent("string")
      }

      assertThat(exception.message).isEqualTo("Unknown DataSet ${dataProcessorMessage.datasetId}")

      coVerify(exactly = 0) { eventDataRepository.saveAll(any<Iterable<EventData>>()) }
    }
  }

  private val simpleSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    eventType = "DEATH_NOTIFICATION",
    enrichmentFields = "firstName,lastName",
  )
  private val complexSubscription = ConsumerSubscription(
    consumerId = UUID.randomUUID(),
    eventType = "DEATH_NOTIFICATION",
    enrichmentFields = "firstName,lastName,sex",
  )
  private val consumerSubscriptions = flowOf(simpleSubscription, complexSubscription)
  private val fakeSavedEvents = flowOf(
    EventData(
      consumerSubscriptionId = UUID.randomUUID(),
      datasetId = UUID.randomUUID().toString(),
      dataId = "HMPO",
    ),
    EventData(
      consumerSubscriptionId = UUID.randomUUID(),
      datasetId = UUID.randomUUID().toString(),
      dataId = "HMPO",
    ),
  )
}
