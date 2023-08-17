package uk.gov.di.data.lep.library.dto.DeathNotification;

import java.time.LocalDate;

public record IsoDate(
    String description,
    LocalDate value
) {
}
