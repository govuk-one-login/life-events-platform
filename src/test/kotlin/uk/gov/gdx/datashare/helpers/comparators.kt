package uk.gov.gdx.datashare.uk.gov.gdx.datashare.helpers

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

val compareIgnoringNanos: Comparator<LocalDateTime> =
  Comparator { d1, d2 -> ChronoUnit.MICROS.between(d1, d2).toInt() }

