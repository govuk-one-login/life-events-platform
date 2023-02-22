package uk.gov.gdx.datashare.services

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.repositories.EventDataRepository
import java.util.concurrent.TimeUnit

@Service
class ScheduledJobService(
  private val eventDataRepository: EventDataRepository,
  private val meterRegistry: MeterRegistry,
) {
  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  @SchedulerLock(name = "countUnconsumedEvents", lockAtMostFor = "3m", lockAtLeastFor = "3m")
  fun countUnconsumedEvents() {
    LockAssert.assertLocked()
    EventDataService.log.debug("Looking for unconsumed events")
    val unconsumedEventsCount = eventDataRepository.countByDeletedAtIsNull()
    Gauge.builder("UnconsumedEvents", unconsumedEventsCount, Int::toDouble).register(meterRegistry)
  }
}
