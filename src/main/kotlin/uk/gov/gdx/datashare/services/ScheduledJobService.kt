package uk.gov.gdx.datashare.services

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.helpers.repeatForLimitedTime
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Service
class ScheduledJobService(
  private val acquirerEventRepository: AcquirerEventRepository,
  private val groApiService: GroApiService,
  private val supplierEventRepository: SupplierEventRepository,
  private val meterRegistry: MeterRegistry,
  private val outboundEventQueueService: OutboundEventQueueService,
) {
  private val unconsumedEventsGauge =
    meterRegistry.gauge("UnconsumedEvents", AtomicInteger(acquirerEventRepository.countByDeletedAtIsNull()))

  @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
  fun countUnconsumedEvents() {
    val unconsumedEventsCount = acquirerEventRepository.countByDeletedAtIsNull()
    unconsumedEventsGauge!!.set(unconsumedEventsCount)
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  final fun monitorQueueMetrics() {
    val queueMetrics = outboundEventQueueService.getMetrics()
    queueMetrics.forEach {
      meterRegistry.gauge("dlq_length", listOf(Tag.of("queue_name", it.key)), AtomicInteger(it.value.dlqLength))
      meterRegistry.gauge("age_of_oldest_message", listOf(Tag.of("queue_name", it.key)), AtomicInteger(it.value.ageOfOldestMessage))
    }
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  @SchedulerLock(name = "deleteConsumedGroSupplierEvents", lockAtMostFor = "60s", lockAtLeastFor = "15s")
  fun deleteConsumedGroSupplierEvents() {
    LockAssert.assertLocked()
    val events = supplierEventRepository.findGroDeathEventsForDeletion()
    events.shuffle()
    repeatForLimitedTime(
      events,
      groApiService::deleteConsumedGroSupplierEvent,
    )
  }
}
