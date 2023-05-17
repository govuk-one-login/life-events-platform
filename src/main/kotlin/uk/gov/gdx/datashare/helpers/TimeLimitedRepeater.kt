package uk.gov.gdx.datashare.helpers

import java.time.LocalDateTime

class TimeLimitedRepeater {
  companion object {

    private const val defaultTimeLimit: Long = 45
    fun <T> repeat(items: List<T>, repeatableFunction: (item: T) -> Unit, timeLimitInSeconds: Long? = null) {
      val startTime = LocalDateTime.now()
      items.forEach {
        repeatableFunction(it)
        if (LocalDateTime.now() > startTime.plusSeconds(timeLimitInSeconds ?: defaultTimeLimit)) {
          return
        }
      }
    }
  }
}
