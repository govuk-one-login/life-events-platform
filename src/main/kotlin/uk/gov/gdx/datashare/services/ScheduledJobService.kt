package uk.gov.gdx.datashare.services

import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Service
class ScheduledJobService(
  private val acquirerEventRepository: AcquirerEventRepository,
  meterRegistry: MeterRegistry,
) {
  private val gauge = meterRegistry.gauge("UnconsumedEvents", AtomicInteger(acquirerEventRepository.countByDeletedAtIsNull()))

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  @SchedulerLock(name = "countUnconsumedEvents", lockAtMostFor = "3m", lockAtLeastFor = "3m")
  fun countUnconsumedEvents() {
    LockAssert.assertLocked()
    AcquirerEventService.log.debug("Looking for unconsumed events")
    val unconsumedEventsCount = acquirerEventRepository.countByDeletedAtIsNull()
    gauge!!.set(unconsumedEventsCount)
  }
}
