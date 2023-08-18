package uk.gov.di.data.lep.library.dto.deathnotification;

import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.GroPersonNameStructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DeathNotificationSetMapperTest {
    @Test
    void mapperMapsNewEventCorrectly() {
        var eventTime = LocalDateTime.parse("2021-03-06T09:30:50");
        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder()
                .withLockedDateTime(eventTime)
                .withUpdateDateTime(null)
                .withUpdateReason(null)
                .build()
        );

        assertNotNull(actual.events().deathRegistrationEvent());
        assertNull(actual.events().deathRegistrationUpdateEvent());

        assertEquals(eventTime, actual.events().deathRegistrationEvent().deathRegistrationTime().value());
        assertNull(actual.events().deathRegistrationEvent().recordUpdateTime());
        assertNull(actual.events().deathRegistrationEvent().deathRegistrationUpdateReason());
        assertEquals(eventTime.toEpochSecond(ZoneOffset.UTC), actual.toe());
    }

    @Test
    void mapperSetsAllValuesCorrectlyForUpdateEvent() {
        var eventTime = LocalDateTime.parse("2022-03-06T09:30:50");
        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder()
                .withLockedDateTime(null)
                .withUpdateDateTime(eventTime)
                .withUpdateReason(5)
                .build()
        );

        assertNull(actual.events().deathRegistrationEvent());
        assertNotNull(actual.events().deathRegistrationUpdateEvent());

        assertNull(actual.events().deathRegistrationUpdateEvent().deathRegistrationTime());
        assertEquals(eventTime, actual.events().deathRegistrationUpdateEvent().recordUpdateTime().value());
        assertEquals(DeathRegistrationUpdateReasonType.CANCELLATION_REMOVED, actual.events().deathRegistrationUpdateEvent().deathRegistrationUpdateReason());
        assertEquals(eventTime.toEpochSecond(ZoneOffset.UTC), actual.toe());
    }

    @Test
    void mapperMapsCurrentNameCorrectly() {
        var name = new GroPersonNameStructure(
            "Title",
            List.of("Bob", "Rob", "Robert"),
            "Smith",
            "Suffix",
            "RequestedName"
        );
        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder()
                .withName(name)
                .withAliases(null, null)
                .withMaidenName(null)
                .build()
        );
        var actualName = actual.events().deathRegistrationEvent().subject().name().get(0);
        var actualFamilyName = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualGivenNames = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertNull(actualName.description());
        assertNull(actualName.validFrom());
        assertNull(actualName.validUntil());
        assertEquals(1, actual.events().deathRegistrationEvent().subject().name().size());

        assertEquals(name.personFamilyName(), actualFamilyName);
        assertEquals(name.personGivenNames(), actualGivenNames);
        assertEquals(4, actualName.nameParts().size());
    }

    @Test
    void mapperMapsMaidenNameCorrectly() {
        var name = new GroPersonNameStructure(
            "Title",
            List.of("Bob", "Rob", "Robert"),
            "Smith",
            "Suffix",
            "RequestedName"
        );
        var maidenName = "Jones";
        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder()
                .withName(name)
                .withAliases(null, null)
                .withMaidenName(maidenName)
                .build()
        );
        var actualNames = actual.events().deathRegistrationEvent().subject().name();

        var actualName = actualNames.stream().filter(n -> n.description() == null).toList().get(0);
        var actualFamilyName = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualGivenNames = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertNull(actualName.description());
        assertNull(actualName.validFrom());
        assertNull(actualName.validUntil());

        assertEquals(name.personFamilyName(), actualFamilyName);
        assertEquals(name.personGivenNames(), actualGivenNames);
        assertEquals(4, actualName.nameParts().size());

        var actualMaidenName = actualNames.stream().filter(n -> Objects.equals(n.description(), "Name before marriage")).toList().get(0);
        var actualMaidenFamilyName = actualMaidenName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualMaidenGivenNames = actualMaidenName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertEquals("Name before marriage", actualMaidenName.description());
        assertNull(actualMaidenName.validFrom());
        assertNull(actualMaidenName.validUntil());

        assertEquals(maidenName, actualMaidenFamilyName);
        assertEquals(name.personGivenNames(), actualMaidenGivenNames);
        assertEquals(4, actualName.nameParts().size());
    }

    @Test
    void mapperMapsAliasNamesCorrectly() {
        var name = new GroPersonNameStructure(
            "Title",
            List.of("Bob", "Rob"),
            "Smith",
            "Suffix",
            "RequestedName"
        );
        var firstAliasName = new GroPersonNameStructure(
            null,
            List.of("Alias1"),
            "AliasSurname1",
            null,
            null
        );
        var secondAliasName = new GroPersonNameStructure(
            null,
            List.of("Alias2"),
            "AliasSurname2",
            null,
            null
        );
        var aliasNames = List.of(firstAliasName, secondAliasName);
        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(
            new GroJsonRecordBuilder()
                .withName(name)
                .withAliases(aliasNames, List.of("Alias type 1"))
                .withMaidenName(null)
                .build()
        );
        var actualNames = actual.events().deathRegistrationEvent().subject().name();

        var actualName = actualNames.stream().filter(n -> n.nameParts().size() == 3).toList().get(0);
        var actualFamilyName = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualGivenNames = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertNull(actualName.description());
        assertNull(actualName.validFrom());
        assertNull(actualName.validUntil());

        assertEquals(name.personFamilyName(), actualFamilyName);
        assertEquals(name.personGivenNames(), actualGivenNames);
        assertEquals(3, actualName.nameParts().size());

        var actualFirstAliasName = actualNames.stream().filter(n -> n.nameParts().size() == 2 && Objects.equals(n.description(), "Alias type 1")).toList().get(0);
        var actualFirstAliasFamilyName = actualFirstAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualFirstAliasGivenNames = actualFirstAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertEquals("Alias type 1", actualFirstAliasName.description());
        assertNull(actualFirstAliasName.validFrom());
        assertNull(actualFirstAliasName.validUntil());

        assertEquals("AliasSurname1", actualFirstAliasFamilyName);
        assertEquals(List.of("Alias1"), actualFirstAliasGivenNames);
        assertEquals(2, actualFirstAliasName.nameParts().size());

        var actualSecondAliasName = actualNames.stream().filter(n -> n.nameParts().size() == 2 && n.description() == null).toList().get(0);
        var actualSecondAliasFamilyName = actualSecondAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualSecondAliasGivenNames = actualSecondAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertNull(actualSecondAliasName.description());
        assertNull(actualSecondAliasName.validFrom());
        assertNull(actualSecondAliasName.validUntil());

        assertEquals("AliasSurname2", actualSecondAliasFamilyName);
        assertEquals(List.of("Alias2"), actualSecondAliasGivenNames);
        assertEquals(2, actualSecondAliasName.nameParts().size());

    }

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
