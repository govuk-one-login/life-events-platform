package uk.gov.gdx.datashare.services

import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.gdx.datashare.helpers.TimeLimitedRepeater
import uk.gov.gdx.datashare.repositories.AcquirerEventRepository
import uk.gov.gdx.datashare.repositories.SupplierEventRepository
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Service
class ScheduledJobService(
  private val acquirerEventRepository: AcquirerEventRepository,
  private val groApiService: GroApiService,
  private val supplierEventRepository: SupplierEventRepository,
  meterRegistry: MeterRegistry,
) {
  private val gauge =
    meterRegistry.gauge("UnconsumedEvents", AtomicInteger(acquirerEventRepository.countByDeletedAtIsNull()))

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  @SchedulerLock(name = "countUnconsumedEvents", lockAtMostFor = "3m", lockAtLeastFor = "3m")
  fun countUnconsumedEvents() {
    LockAssert.assertLocked()
    val unconsumedEventsCount = acquirerEventRepository.countByDeletedAtIsNull()
    gauge!!.set(unconsumedEventsCount)
  }

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  @SchedulerLock(name = "deleteConsumedGroSupplierEvents", lockAtMostFor = "45s", lockAtLeastFor = "15s")
  fun deleteConsumedGroSupplierEvents() {
    LockAssert.assertLocked()
    TimeLimitedRepeater.repeat(
      supplierEventRepository.findGroDeathEventsForDeletion(),
      groApiService::deleteConsumedGroSupplierEvent,
    )
  }
}
