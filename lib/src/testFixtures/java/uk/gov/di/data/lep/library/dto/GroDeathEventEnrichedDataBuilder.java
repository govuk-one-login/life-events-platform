package uk.gov.di.data.lep.library.dto;

import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GroDeathEventEnrichedDataBuilder {
    public final GroDeathEventEnrichedData data;

    public GroDeathEventEnrichedDataBuilder() {
        data = new GroDeathEventEnrichedData(
            123456789,
            GenderAtRegistration.FEMALE,
            LocalDate.parse("1972-02-20"),
            LocalDate.parse("2021-12-31"),
            123456789,
            LocalDateTime.parse("2022-01-05T12:03:52"),
            12,
            2021,
            List.of("Bob", "Burt"),
            "Smith",
            "Jane",
            "888",
            "Death House",
            List.of("8 Death lane", "Deadington", "Deadshire"),
            "XX1 1XX"
        );
    }

    public GroDeathEventEnrichedData build() {
        return data;
    }
}
