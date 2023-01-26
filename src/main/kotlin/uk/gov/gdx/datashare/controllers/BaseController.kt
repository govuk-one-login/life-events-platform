package uk.gov.gdx.datashare.controllers

import io.micrometer.core.instrument.Counter

interface BaseController {
  fun <T> tryCallAndUpdateMetric(
    call: () -> T,
    successCounter: Counter,
    failureCounter: Counter,
  ): T {
    try {
      val result = call()
      successCounter.increment()
      return result
    } catch (e: Exception) {
      failureCounter.increment()
      throw e
    }
  }
}
