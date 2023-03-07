package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.enums.EventType
import uk.gov.gdx.datashare.models.DataProcessorMessage
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.services.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class DataProcessorTest {
  private val objectMapper = mockk<ObjectMapper>()
  private val acquirerSubscriptionRepository = mockk<AcquirerSubscriptionRepository>()
  private val eventDataRepository = mockk<EventDataRepository>()

  private val underTest: DataProcessor = DataProcessor(
    objectMapper,
    acquirerSubscriptionRepository,
    eventDataRepository,
  )

  private val ONE_SECOND_OFFSET = TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)

  @Test
  fun `onGovEvent saves death notifications for LEV`() {
    val dataProcessorMessage = DataProcessorMessage(
      eventType = EventType.DEATH_NOTIFICATION,
      eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
      supplier = "HMPO",
      subscriptionId = UUID.randomUUID(),
      id = "123456789",
    )

    every { acquirerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventType) }
      .returns(acquirerSubscriptions)
    every { eventDataRepository.saveAll(any<Iterable<EventData>>()) }.returns(fakeSavedEvents)
    every { objectMapper.readValue(any<String>(), DataProcessorMessage::class.java) }.returns(dataProcessorMessage)

    underTest.onGovEvent("string")

    verify(exactly = 1) {
      eventDataRepository.saveAll(
        withArg<Iterable<EventData>> {
          assertThat(it).hasSize(2)

          it.forEach { event ->
            assertThat(event.dataId).isEqualTo(dataProcessorMessage.id)
            assertThat(event.eventTime).isEqualTo(dataProcessorMessage.eventTime)
            assertThat(event.whenCreated).isCloseToUtcNow(ONE_SECOND_OFFSET)
          }
        },
      )
    }
  }

  @Test
  fun `onGovEvent saves death notifications for PASS_THROUGH`() {
    val dataProcessorMessage = DataProcessorMessage(
      eventType = EventType.DEATH_NOTIFICATION,
      eventTime = LocalDateTime.of(2010, 1, 1, 12, 0),
      supplier = "HMPO",
      subscriptionId = UUID.randomUUID(),
      id = "123456789",
    )

    every { acquirerSubscriptionRepository.findAllByEventType(dataProcessorMessage.eventType) }
      .returns(acquirerSubscriptions)
    every { eventDataRepository.saveAll(any<Iterable<EventData>>()) }.returns(fakeSavedEvents)
    every { objectMapper.readValue(any<String>(), DataProcessorMessage::class.java) }.returns(dataProcessorMessage)

    underTest.onGovEvent("string")

    verify(exactly = 1) {
      eventDataRepository.saveAll(
        withArg<Iterable<EventData>> {
          assertThat(it).hasSize(2)

          it.forEach { event ->
            assertThat(event.dataId).isEqualTo(dataProcessorMessage.id)
            assertThat(event.eventTime).isEqualTo(dataProcessorMessage.eventTime)
            assertThat(event.whenCreated).isCloseToUtcNow(ONE_SECOND_OFFSET)
          }
        },
      )
    }
  }

  private val acquirerSubscriptions = listOf(
    AcquirerSubscription(
      acquirerId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
    ),
    AcquirerSubscription(
      acquirerId = UUID.randomUUID(),
      eventType = EventType.DEATH_NOTIFICATION,
    ),
  )

  private val fakeSavedEvents = listOf(
    EventData(
      acquirerSubscriptionId = UUID.randomUUID(),
      dataId = "HMPO",
    ),
    EventData(
      acquirerSubscriptionId = UUID.randomUUID(),
      dataId = "HMPO",
    ),
  )
}
