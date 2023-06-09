package uk.gov.gdx.datashare.services

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.helpers.repeatForLimitedTime
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.AcquirerRepository
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Service
class ScheduledJobService(
  private val acquirerEventRepository: AcquirerEventRepository,
  private val acquirerRepository: AcquirerRepository,
  private val groApiService: GroApiService,
  private val supplierEventRepository: SupplierEventRepository,
  private val meterRegistry: MeterRegistry,
  private val outboundEventQueueService: OutboundEventQueueService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private val queueMetersMap = mutableMapOf<String, Pair<AtomicInteger, AtomicInteger>>()
  private val unconsumedEventsMetersMap = mutableMapOf<UUID, AtomicInteger>()

  @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
  fun countUnconsumedEvents() {
    val unconsumedEventsForSubscriptions = acquirerEventRepository.countByDeletedAtIsNullForSubscriptions()

    unconsumedEventsMetersMap.forEach { meter ->
      if (!unconsumedEventsForSubscriptions.map { it.acquirerSubscriptionId }.contains(meter.key)) {
        unconsumedEventsMetersMap.remove(meter.key)
      }
    }

    unconsumedEventsForSubscriptions.forEach { (subscription, count) ->
      val meter = unconsumedEventsMetersMap.computeIfAbsent(subscription) {
        meterRegistry.gauge(
          "unconsumed_events",
          listOf(
            Tag.of("acquirer_subscription_id", it.toString()),
            Tag.of("acquirer", acquirerRepository.findNameForAcquirerSubscriptionId(it)),
          ),
          AtomicInteger(0),
        )!!
      }
      meter.set(count)
    }
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  fun monitorQueueMetrics() {
    val queueMetrics = outboundEventQueueService.getMetrics()

    queueMetersMap.forEach {
      if (!queueMetrics.keys.contains(it.key)) {
        queueMetersMap.remove(it.key)
      }
    }

    queueMetrics.forEach { (queueName, queueMetric) ->
      val (ageMeter, dlqLengthMeter) = queueMetersMap.computeIfAbsent(queueName) {
        Pair(
          meterRegistry.gauge("age_of_oldest_message", listOf(Tag.of("queue_name", it)), AtomicInteger(0))!!,
          meterRegistry.gauge("dlq_length", listOf(Tag.of("queue_name", it)), AtomicInteger(0))!!,
        )
      }
      ageMeter.set(queueMetric.ageOfOldestMessage ?: 0)
      if (queueMetric.ageOfOldestMessage == null) {
        log.warn("No age_of_oldest_message found for queue: $queueName")
      }
      dlqLengthMeter.set(queueMetric.dlqLength ?: 0)
      if (queueMetric.dlqLength == null) {
        log.warn("No dlq_length found for queue: $queueName")
      }
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
