package uk.gov.gdx.datashare.services

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.mockk.*
import net.javacrumbs.shedlock.core.LockAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import uk.gov.gdx.datashare.repositories.*
import uk.gov.gdx.datashare.services.GroApiService
import uk.gov.gdx.datashare.services.OutboundEventQueueService
import uk.gov.gdx.datashare.services.QueueMetric
import uk.gov.gdx.datashare.services.ScheduledJobService
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ScheduledJobServiceTest {
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
  private val acquirerRepository = mockk<AcquirerRepository>()
  private val groApiService = mockk<GroApiService>()
  private val supplierEventRepository = mockk<SupplierEventRepository>()
  private val meterRegistry = mockk<MeterRegistry>()
  private val outboundEventQueueService = mockk<OutboundEventQueueService>()
  private val listAppender = ListAppender<ILoggingEvent>()

  private lateinit var underTest: ScheduledJobService

  @BeforeEach
  fun init() {
    val logger = LoggerFactory.getLogger(ScheduledJobService::class.java) as Logger
    listAppender.start()
    logger.addAppender(listAppender)

    mockkStatic(LockAssert::class)
    every { LockAssert.assertLocked() } just runs
    underTest = ScheduledJobService(
      acquirerEventRepository,
      acquirerRepository,
      groApiService,
      supplierEventRepository,
      meterRegistry,
      outboundEventQueueService,
    )
  }

  @Test
  fun `countUnconsumedEvents monitors unconsumed events`() {
    val acquirerNameOne = "acquirerone"
    val acquirerNameTwo = "acquirertwo"
    val subscriptionOneId = UUID.randomUUID()
    val subscriptionTwoId = UUID.randomUUID()
    val subscriptionThreeId = UUID.randomUUID()
    val metrics = listOf(
      SubscriptionsCount(subscriptionOneId, 1),
      SubscriptionsCount(subscriptionTwoId, 2),
      SubscriptionsCount(subscriptionThreeId, 3),
    )
    val unconsumedEventMeterOne = setupSubscriptionMeter(subscriptionOneId, acquirerNameOne)
    val unconsumedEventMeterTwo = setupSubscriptionMeter(subscriptionTwoId, acquirerNameOne)
    val unconsumedEventMeterThree = setupSubscriptionMeter(subscriptionThreeId, acquirerNameTwo)
    every { acquirerEventRepository.countByDeletedAtIsNullForSubscriptions() } returns metrics

    underTest.countUnconsumedEvents()

    verify(exactly = 1) {
      unconsumedEventMeterOne.set(1)
      unconsumedEventMeterTwo.set(2)
      unconsumedEventMeterThree.set(3)
    }
  }

  @Test
  fun `countUnconsumedEvents removes strong references`() {
    val acquirerNameOne = "acquirerone"
    val acquirerNameTwo = "acquirertwo"
    val subscriptionOneId = UUID.randomUUID()
    val subscriptionTwoId = UUID.randomUUID()
    val subscriptionThreeId = UUID.randomUUID()
    val firstMetrics = listOf(
      SubscriptionsCount(subscriptionOneId, 0),
      SubscriptionsCount(subscriptionTwoId, 2),
      SubscriptionsCount(subscriptionThreeId, 3),
    )
    val secondMetrics = listOf(
      SubscriptionsCount(subscriptionOneId, 11),
      SubscriptionsCount(subscriptionTwoId, 12),
    )
    val unconsumedEventMeterOne = setupSubscriptionMeter(subscriptionOneId, acquirerNameOne)
    val unconsumedEventMeterTwo = setupSubscriptionMeter(subscriptionTwoId, acquirerNameOne)
    val unconsumedEventMeterThree = setupSubscriptionMeter(subscriptionThreeId, acquirerNameTwo)
    every { acquirerEventRepository.countByDeletedAtIsNullForSubscriptions() } returns firstMetrics andThen secondMetrics

    underTest.countUnconsumedEvents()
    underTest.countUnconsumedEvents()

    verify(exactly = 1) {
      unconsumedEventMeterOne.set(0)
      unconsumedEventMeterTwo.set(2)
      unconsumedEventMeterThree.set(3)

      unconsumedEventMeterOne.set(11)
      unconsumedEventMeterTwo.set(12)

      unconsumedEventMeterThree.set(any())
    }
  }

  @Test
  fun `scheduledMonitorQueueMetrics monitors queue metrics`() {
    val metrics = mapOf(
      "queueone" to QueueMetric(ageOfOldestMessage = 1, dlqLength = 11),
      "queuetwo" to QueueMetric(ageOfOldestMessage = 2, dlqLength = null),
      "queuethree" to QueueMetric(ageOfOldestMessage = null, dlqLength = 13),
    )
    val ageMeterOne = setupQueueMeter("age_of_oldest_message", "queueone")
    val ageMeterTwo = setupQueueMeter("age_of_oldest_message", "queuetwo")
    val ageMeterThree = setupQueueMeter("age_of_oldest_message", "queuethree")
    val dlqLengthMeterOne = setupQueueMeter("dlq_length", "queueone")
    val dlqLengthMeterTwo = setupQueueMeter("dlq_length", "queuetwo")
    val dlqLengthMeterThree = setupQueueMeter("dlq_length", "queuethree")
    every { outboundEventQueueService.getMetrics() } returns metrics

    underTest.monitorQueueMetrics()

    verify(exactly = 1) {
      ageMeterOne.set(1)
      ageMeterTwo.set(2)
      ageMeterThree.set(0)
      dlqLengthMeterOne.set(11)
      dlqLengthMeterTwo.set(0)
      dlqLengthMeterThree.set(13)
    }

    val logsList = listAppender.list
    assertThat(logsList[0].message).isEqualTo("No age_of_oldest_message found for queue: queuethree")
    assertThat(logsList[0].level).isEqualTo(Level.WARN)
  }

  @Test
  fun `scheduledMonitorQueueMetrics removes strong references`() {
    val firstMetrics = mapOf(
      "queueone" to QueueMetric(ageOfOldestMessage = 1, dlqLength = 11),
      "queuetwo" to QueueMetric(ageOfOldestMessage = 2, dlqLength = 12),
    )
    val secondMetrics = mapOf(
      "queueone" to QueueMetric(ageOfOldestMessage = 101, dlqLength = 111),
    )
    val ageMeterOne = setupQueueMeter("age_of_oldest_message", "queueone")
    val ageMeterTwo = setupQueueMeter("age_of_oldest_message", "queuetwo")
    val dlqLengthMeterOne = setupQueueMeter("dlq_length", "queueone")
    val dlqLengthMeterTwo = setupQueueMeter("dlq_length", "queuetwo")
    every { outboundEventQueueService.getMetrics() } returns firstMetrics andThen secondMetrics

    underTest.monitorQueueMetrics()
    underTest.monitorQueueMetrics()

    verify(exactly = 1) {
      ageMeterOne.set(1)
      ageMeterTwo.set(2)
      dlqLengthMeterOne.set(11)
      dlqLengthMeterTwo.set(12)

      ageMeterOne.set(101)
      dlqLengthMeterOne.set(111)

      ageMeterTwo.set(any())
      dlqLengthMeterTwo.set(any())
    }
  }

  @Test
  fun `deleteConsumedGroSupplierEvents deletes multiple events`() {
    val supplierEvent1 = SupplierEvent(
      supplierSubscriptionId = UUID.randomUUID(),
      dataId = "asdasd",
      eventTime = null,
    )
    val supplierEvent2 = SupplierEvent(
      supplierSubscriptionId = UUID.randomUUID(),
      dataId = "asdasd2",
      eventTime = null,
    )
    val supplierEvents = mutableListOf(supplierEvent1, supplierEvent2)
    every { supplierEventRepository.findGroDeathEventsForDeletion() } returns supplierEvents
    every { groApiService.deleteConsumedGroSupplierEvent(supplierEvent1) } just runs
    every { groApiService.deleteConsumedGroSupplierEvent(supplierEvent2) } just runs

    underTest.deleteConsumedGroSupplierEvents()

    verify(exactly = 1) {
      groApiService.deleteConsumedGroSupplierEvent(supplierEvent1)
    }
    verify(exactly = 1) {
      groApiService.deleteConsumedGroSupplierEvent(supplierEvent2)
    }
  }

  private fun setupSubscriptionMeter(subscriptionId: UUID, acquirerName: String): AtomicInteger {
    val atomicInteger = mockk<AtomicInteger>()
    every {
      meterRegistry.gauge(
        "unconsumed_events",
        listOf(
          Tag.of("acquirer_subscription_id", subscriptionId.toString()),
          Tag.of("acquirer", acquirerName),
        ),
        any<AtomicInteger>(),
      )
    } returns atomicInteger
    every { atomicInteger.set(any()) } just runs
    every { acquirerRepository.findNameForAcquirerSubscriptionId(subscriptionId) } returns acquirerName
    return atomicInteger
  }

  private fun setupQueueMeter(metricName: String, queueName: String): AtomicInteger {
    val atomicInteger = mockk<AtomicInteger>()
    every {
      meterRegistry.gauge(
        metricName,
        listOf(Tag.of("queue_name", queueName)),
        any<AtomicInteger>(),
      )
    } returns atomicInteger
    every { atomicInteger.set(any()) } just runs
    return atomicInteger
  }
}
