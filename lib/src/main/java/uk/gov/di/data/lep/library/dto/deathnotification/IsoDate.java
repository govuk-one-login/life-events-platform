package uk.gov.di.data.lep.library.dto.deathnotification;

import java.time.LocalDate;

public record IsoDate(
    String description,
    LocalDate value
) {
}
