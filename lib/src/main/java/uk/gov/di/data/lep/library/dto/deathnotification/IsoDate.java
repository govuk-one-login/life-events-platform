package uk.gov.di.data.lep.library.dto.deathnotification;

import java.time.temporal.TemporalAccessor;

public record IsoDate(
    String description,
    TemporalAccessor value
) {
}
