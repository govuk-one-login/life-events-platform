package uk.gov.gdx.datashare.helpers

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

fun getHistogramTimer(meterRegistry: MeterRegistry, name: String): Timer = Timer
  .builder(name)
  .publishPercentileHistogram()
  .register(meterRegistry)
