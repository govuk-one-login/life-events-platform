package uk.gov.gdx.datashare.config

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

@Service
class DateTimeHandler(
  private val clock: Clock
) {
  fun now(): LocalDateTime = LocalDateTime.now(clock)

  fun defaultStartTime(): LocalDateTime = LocalDateTime.of(2000, 1, 1, 12, 0)
}