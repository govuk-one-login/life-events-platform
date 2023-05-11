package uk.gov.gdx.datashare.services

import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class ScheduledConsumedEventCheckerService(
  private val eventConsumedCheckingService: EventConsumedCheckingService,
) {

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
  @SchedulerLock(name = "checkConsumedEvents", lockAtMostFor = "3m", lockAtLeastFor = "3m")
  fun checkForConsumedEvents() {
    LockAssert.assertLocked()
    eventConsumedCheckingService.checkAndMarkConsumed()
  }
}
