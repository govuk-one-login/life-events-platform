package uk.gov.di.data.lep.library.dto;

import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GroJsonRecordBuilder {
    private Integer partialYearOfDeath;
    private Integer partialMonthOfDeath;
    private PersonDeathDateStructure personDeathDate;

    public GroJsonRecordBuilder() {
        personDeathDate = new PersonDeathDateStructure(LocalDate.parse("2007-03-06"), null);
    }

    public GroJsonRecordBuilder withDeathDate(LocalDate deathDate) {
        if (deathDate == null) {
            personDeathDate = null;
        } else {
            personDeathDate = new PersonDeathDateStructure(deathDate, null);
        }
        return this;
    }

    public GroJsonRecordBuilder withDeathYear(Integer partialYearOfDeath) {
        this.partialYearOfDeath = partialYearOfDeath;
        return this;
    }

    public GroJsonRecordBuilder withDeathMonth(Integer partialMonthOfDeath) {
        this.partialMonthOfDeath = partialMonthOfDeath;
        return this;
    }

    public GroJsonRecord build() {
        return new GroJsonRecord(
            1234567890,
            1,
            LocalDateTime.parse("2023-03-06T09:30:50"),
            null,
            null,
            new GroPersonNameStructure("Mrs", List.of("ERICA"), "BLOGG", null, null),
            null,
            null,
            null,
            GenderAtRegistration.FEMALE,
            personDeathDate,
            partialMonthOfDeath,
            partialYearOfDeath,
            null,
            null,
            new PersonBirthDateStructure(LocalDate.parse("1967-03-06"), null),
            null,
            null,
            null,
            new GroAddressStructure(null, null, List.of("123 Street"), "GT8 5HG")
        );
    }
}
