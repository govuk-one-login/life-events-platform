package uk.gov.gdx.datashare.helpers

import java.time.LocalDateTime

fun <T> repeatForLimitedTime(items: Iterable<T>, action: (item: T) -> Unit, timeLimitInSeconds: Long = 45) {
  val startTime = LocalDateTime.now()
  items.forEach {
    action(it)
    if (LocalDateTime.now() > startTime.plusSeconds(timeLimitInSeconds)) {
      return
    }
  }
}
