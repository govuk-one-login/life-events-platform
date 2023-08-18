package uk.gov.di.data.lep.library.dto.deathnotification;

import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DeathNotificationSetMapperTest {
    @Test
    void mapperMapsCompleteDateCorrectly() {
        var testDate = LocalDate.parse("2007-03-06");

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder().withDeathDate(testDate).build()
        );

        assertEquals(testDate, actual.events().deathRegistrationEvent().deathDate().value());
    }

    @Test
    void mapperMapsYearOnlyDateCorrectly() {
        var testYear = 2023;

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder()
                .withDeathDate(null)
                .withDeathYear(testYear)
                .build()
        );

        assertEquals(Year.of(2023), actual.events().deathRegistrationEvent().deathDate().value());
    }

    @Test
    void mapperMapsMonthYearDateCorrectly() {
        var testYear = 2023;
        var testMonth = 12;

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder()
                .withDeathDate(null)
                .withDeathYear(testYear)
                .withDeathMonth(testMonth)
                .build()
        );

        assertEquals(YearMonth.of(2023, 12), actual.events().deathRegistrationEvent().deathDate().value());
    }

    @Test
    void mapperMapsMonthOnlyDateCorrectly() {
        var testMonth = 12;

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordBuilder()
            .withDeathDate(null)
            .withDeathYear(null)
            .withDeathMonth(testMonth)
            .build());

        assertNull(actual.events().deathRegistrationEvent().deathDate().value());
    }
}
