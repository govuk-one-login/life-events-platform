package uk.gov.di.data.lep.library.dto;

import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GroJsonRecordBuilder {
    private final GroJsonRecord record;

    public GroJsonRecordBuilder() {
        record = new GroJsonRecord(
            1234567890,
            1,
            LocalDateTime.parse("2023-03-06T09:30:50"),
            LocalDateTime.parse("2023-03-06T09:30:50"),
            1,
            new GroPersonNameStructure("Mrs", List.of("ERICA"), "BLOGG", null, null),
            null,
            null,
            null,
            GenderAtRegistration.FEMALE,
            new PersonDeathDateStructure(LocalDate.parse("2007-03-06"), null),
            3,
            2007,
            null,
            null,
            new PersonBirthDateStructure(LocalDate.parse("1967-03-06"), null),
            3,
            1967,
            null,
            new GroAddressStructure(null, null, List.of("123 Street"), "GT8 5HG")
        );
    }


    public GroJsonRecord build() {
        return record;
    }
}
