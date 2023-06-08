package uk.gov.gdx.datashare.uk.gov.gdx.datashare.services

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
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.SupplierEvent
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import uk.gov.gdx.datashare.services.GroApiService
import uk.gov.gdx.datashare.services.OutboundEventQueueService
import uk.gov.gdx.datashare.services.QueueMetric
import uk.gov.gdx.datashare.services.ScheduledJobService
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ScheduledJobServiceTest {
  private val acquirerEventRepository = mockk<AcquirerEventRepository>()
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

    every { acquirerEventRepository.countByDeletedAtIsNull() } returns 5
    every { meterRegistry.gauge("UnconsumedEvents", any()) } returns AtomicInteger(5)
    every { outboundEventQueueService.getMetrics() } returns emptyMap()
    underTest = ScheduledJobService(
      acquirerEventRepository,
      groApiService,
      supplierEventRepository,
      meterRegistry,
      outboundEventQueueService,
    )
  }

  @Test
  fun `scheduledMonitorQueueMetrics monitors queue metrics`() {
    val metrics = mapOf(
      "queueone" to QueueMetric(ageOfOldestMessage = 1, dlqLength = 11),
      "queuetwo" to QueueMetric(ageOfOldestMessage = 2, dlqLength = null),
      "queuethree" to QueueMetric(ageOfOldestMessage = null, dlqLength = 13),
    )
    every { outboundEventQueueService.getMetrics() } returns metrics
    every { meterRegistry.gauge("dlq_length", any<Iterable<Tag>>(), any<AtomicInteger>()) } returns AtomicInteger(5)
    every {
      meterRegistry.gauge(
        "age_of_oldest_message",
        any<Iterable<Tag>>(),
        any<AtomicInteger>(),
      )
    } returns AtomicInteger(6)

    underTest.monitorQueueMetrics()

    verify(exactly = 1) {
      meterRegistry.gauge(
        "dlq_length",
        listOf(Tag.of("queue_name", "queueone")),
        withArg<AtomicInteger> { assertThat(it.get()).isEqualTo(11) },
      )
      meterRegistry.gauge(
        "age_of_oldest_message",
        listOf(Tag.of("queue_name", "queueone")),
        withArg<AtomicInteger> { assertThat(it.get()).isEqualTo(1) },
      )
      meterRegistry.gauge(
        "age_of_oldest_message",
        listOf(Tag.of("queue_name", "queuetwo")),
        withArg<AtomicInteger> { assertThat(it.get()).isEqualTo(2) },
      )
      meterRegistry.gauge(
        "dlq_length",
        listOf(Tag.of("queue_name", "queuethree")),
        withArg<AtomicInteger> { assertThat(it.get()).isEqualTo(13) },
      )
    }

    val logsList = listAppender.list
    assertThat(logsList[0].message).isEqualTo("No dlq_length found for queue: queuetwo")
    assertThat(logsList[0].level).isEqualTo(Level.ERROR)
    assertThat(logsList[1].message).isEqualTo("No age_of_oldest_message found for queue: queuethree")
    assertThat(logsList[1].level).isEqualTo(Level.ERROR)
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
}
