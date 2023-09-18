package uk.gov.di.data.lep.library.dto.deathnotification;

import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithCorrelationID;
import uk.gov.di.data.lep.library.dto.gro.GroPersonNameStructure;
import uk.gov.di.data.lep.library.exceptions.MappingException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeathNotificationSetMapperTest {
    private final GroPersonNameStructure name = new GroPersonNameStructure(
        "Title",
        List.of("Bob", "Rob", "Robert"),
        "Smith",
        "Suffix"
    );

    @Test
    void mapperMapsNewEventCorrectly() {
        var eventTime = OffsetDateTime.parse("2021-03-06T09:30:50Z");
        var correlationID = "correlationID";
        var groJsonRecord = new GroJsonRecordBuilder()
            .withLockedDateTime(eventTime.toLocalDateTime())
            .withUpdateDateTime(null)
            .withUpdateReason(null)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, correlationID));

        assertEquals(DeathRegisteredEvent.class, actual.events().getClass());
        var event = (DeathRegisteredEvent) actual.events();

        assertEquals(eventTime, event.deathRegistrationTime());
        assertEquals(eventTime.toEpochSecond(), actual.toe());
        assertEquals(correlationID, actual.txn());
    }

    @Test
    void mapperSetsAllValuesCorrectlyForUpdateEvent() {
        var eventTime = OffsetDateTime.parse("2022-03-06T09:30:50Z");
        var correlationID = "correlationID";
        var groJsonRecord = new GroJsonRecordBuilder()
            .withLockedDateTime(null)
            .withUpdateDateTime(eventTime.toLocalDateTime())
            .withUpdateReason(5)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, correlationID));

        assertEquals(DeathRegistrationUpdatedEvent.class, actual.events().getClass());
        var event = (DeathRegistrationUpdatedEvent) actual.events();

        assertEquals(eventTime, event.recordUpdateTime());
        assertEquals(DeathRegistrationUpdateReasonType.CANCELLATION_REMOVED, event.deathRegistrationUpdateReason());
        assertEquals(eventTime.toEpochSecond(), actual.toe());
    }

    @Test
    void mapperThrowsErrorIfBothLockedAndUpdateTimeAreGiven() {
        var eventTime = OffsetDateTime.parse("2022-03-06T09:30:50Z");
        var groJsonRecord = new GroJsonRecordBuilder()
            .withLockedDateTime(eventTime.toLocalDateTime())
            .withUpdateDateTime(eventTime.toLocalDateTime())
            .withUpdateReason(5)
            .build();
        var exception = assertThrows(MappingException.class, () -> DeathNotificationSetMapper.generateDeathNotificationSet(groJsonRecord));
        assertEquals("Record has both recordLocked and recordUpdate dateTimes", exception.getMessage());
    }

    @Test
    void mapperThrowsErrorIfNeitherLockedOrUpdateTimeAreGiven() {
        var groJsonRecord = new GroJsonRecordBuilder()
            .withLockedDateTime(null)
            .withUpdateDateTime(null)
            .withUpdateReason(null)
            .build();
        var exception = assertThrows(MappingException.class, () -> DeathNotificationSetMapper.generateDeathNotificationSet(groJsonRecord));
        assertEquals("Record has neither recordLocked and recordUpdate dateTimes", exception.getMessage());
    }

    @Test
    void mapperMapsCurrentNameCorrectly() {
        var groJsonRecord = new GroJsonRecordBuilder()
            .withName(name)
            .withAliases(null, null)
            .withMaidenName(null)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));
        var actualName = actual.events().subject().name().get(0);
        var actualFamilyName = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualGivenNames = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertNull(actualName.description());
        assertNull(actualName.validFrom());
        assertNull(actualName.validUntil());
        assertEquals(1, actual.events().subject().name().size());

        assertEquals(name.personFamilyName(), actualFamilyName);
        assertEquals(name.personGivenNames(), actualGivenNames);
        assertEquals(4, actualName.nameParts().size());
    }

    @Test
    void mapperMapsMaidenNameCorrectly() {
        var maidenName = "Jones";
        var groJsonRecord = new GroJsonRecordBuilder()
            .withName(name)
            .withAliases(null, null)
            .withMaidenName(maidenName)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));
        var actualNames = actual.events().subject().name();

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
        var firstAliasName = new GroPersonNameStructure(
            null,
            List.of("Alias1"),
            "AliasSurname1",
            null
        );
        var secondAliasName = new GroPersonNameStructure(
            "Mr",
            List.of("Alias2"),
            "AliasSurname2",
            null
        );
        var aliasNames = List.of(firstAliasName, secondAliasName);
        var groJsonRecord = new GroJsonRecordBuilder()
            .withName(name)
            .withAliases(aliasNames, List.of("Alias type 1", "Alias name type 2"))
            .withMaidenName(null)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));
        var actualNames = actual.events().subject().name();

        var actualName = actualNames.stream().filter(n -> n != null && n.nameParts().size() == 4).toList().get(0);
        var actualFamilyName = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualGivenNames = actualName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertNull(actualName.description());
        assertNull(actualName.validFrom());
        assertNull(actualName.validUntil());
        assertEquals(name.personFamilyName(), actualFamilyName);
        assertEquals(name.personGivenNames(), actualGivenNames);
        assertEquals(4, actualName.nameParts().size());

        var actualFirstAliasName = actualNames.stream().filter(n -> n != null && n.nameParts().size() == 2 && Objects.equals(n.description(), "Alias type 1")).toList().get(0);
        var actualFirstAliasFamilyName = actualFirstAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();
        var actualFirstAliasGivenNames = actualFirstAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertEquals("Alias type 1", actualFirstAliasName.description());
        assertEquals("AliasSurname1", actualFirstAliasFamilyName);
        assertEquals(List.of("Alias1"), actualFirstAliasGivenNames);
        assertEquals(2, actualFirstAliasName.nameParts().size());

        var actualSecondAliasName = actualNames.stream().filter(n -> n != null && n.nameParts().size() == 2 && Objects.equals(n.description(), "Alias name type 2")).toList().get(0);
        var actualSecondAliasGivenNames = actualSecondAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertEquals("Alias name type 2", actualSecondAliasName.description());
        assertEquals(List.of("Alias2"), actualSecondAliasGivenNames);
        assertEquals(2, actualSecondAliasName.nameParts().size());
    }

    @Test
    void mapperMapsGivenNameOnlyAliasNamesCorrectly() {
        var aliasGivenName = new GroPersonNameStructure(
            null,
            List.of("GivenNameAlias"),
            null,
            null
        );
        var aliasNames = List.of(aliasGivenName);
        var groJsonRecord = new GroJsonRecordBuilder()
            .withName(name)
            .withAliases(aliasNames, List.of("Alias only given name"))
            .withMaidenName(null)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));
        var actualNames = actual.events().subject().name();

        var actualAliasName = actualNames.stream().filter(n -> n != null && n.nameParts().size() == 1 && Objects.equals(n.description(), "Alias only given name")).toList().get(0);
        var actualAliasGivenNames = actualAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList();

        assertEquals("Alias only given name", actualAliasName.description());
        assertEquals(0, actualAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().size());
        assertEquals(List.of("GivenNameAlias"), actualAliasGivenNames);
        assertEquals(1, actualAliasName.nameParts().size());

    }

    @Test
    void mapperMapsFamilyNameOnlyAliasNamesCorrectly() {
        var aliasFamilyName = new GroPersonNameStructure(
            null,
            null,
            "FamilyNameAlias",
            null
        );
        var aliasNames = List.of(aliasFamilyName);
        var groJsonRecord = new GroJsonRecordBuilder()
            .withName(name)
            .withAliases(aliasNames, List.of("Alias only family name"))
            .withMaidenName(null)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));
        var actualNames = actual.events().subject().name();

        var actualAliasName = actualNames.stream().filter(n -> n != null && n.nameParts().size() == 1 && Objects.equals(n.description(), "Alias only family name")).toList().get(0);
        var actualAliasFamilyName = actualAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.FAMILY_NAME).toList().get(0).value();

        assertEquals("Alias only family name", actualAliasName.description());
        assertEquals("FamilyNameAlias", actualAliasFamilyName);
        assertEquals(0, actualAliasName.nameParts().stream().filter(n -> n.type() == NamePartType.GIVEN_NAME).map(NamePart::value).toList().size());
        assertEquals(1, actualAliasName.nameParts().size());
    }

    @Test
    void mapperMapsTitleOrSuffixOnlyAliasNamesCorrectly() {
        var aliasNameTitle = new GroPersonNameStructure(
            "Dr4",
            null,
            null,
            null
        );
        var aliasNameSuffix = new GroPersonNameStructure(
            null,
            null,
            null,
            "PhD"
        );
        var aliasNames = List.of(aliasNameTitle, aliasNameSuffix);
        var groJsonRecord = new GroJsonRecordBuilder()
            .withName(name)
            .withAliases(aliasNames, List.of("Alias only name title", "Alias only name suffix"))
            .withMaidenName(null)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));
        var actualNames = actual.events().subject().name();
        var actualName = actualNames.stream().filter(n -> n != null && (Objects.equals(n.description(), "Alias only name title") || Objects.equals(n.description(), "Alias only name suffix"))).toList();

        assertEquals(0, actualName.size());
    }

    @Test
    void mapperMapsCompleteDeathDateCorrectly() {
        var testDate = LocalDate.parse("2007-03-06");
        var groJsonRecord = new GroJsonRecordBuilder().withDeathDate(testDate).build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));

        assertEquals(testDate, actual.events().deathDate().value());
    }

    @Test
    void mapperMapsYearOnlyDeathDateCorrectly() {
        var testYear = 2023;
        var groJsonRecord = new GroJsonRecordBuilder()
            .withDeathDate(null)
            .withDeathYear(testYear)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));

        assertEquals(Year.of(2023), actual.events().deathDate().value());
    }

    @Test
    void mapperMapsMonthYearDeathDateCorrectly() {
        var testYear = 2023;
        var testMonth = 12;
        var groJsonRecord = new GroJsonRecordBuilder()
            .withDeathDate(null)
            .withDeathYear(testYear)
            .withDeathMonth(testMonth)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));

        assertEquals(YearMonth.of(2023, 12), actual.events().deathDate().value());
    }

    @Test
    void mapperMapsMonthOnlyDeathDateCorrectly() {
        var testMonth = 12;
        var groJsonRecord = new GroJsonRecordBuilder()
            .withDeathDate(null)
            .withDeathYear(null)
            .withDeathMonth(testMonth)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));
        assertNull(actual.events().deathDate().value());
    }

    @Test
    void mapperMapsCompleteBirthDateCorrectly() {
        var testDate = LocalDate.parse("2007-03-06");
        var groJsonRecord = new GroJsonRecordBuilder().withBirthDate(testDate).build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));

        assertEquals(testDate, actual.events().subject().birthDate().get(0).value());
    }

    @Test
    void mapperMapsYearOnlyBirthDateCorrectly() {
        var testYear = 2023;
        var groJsonRecord = new GroJsonRecordBuilder()
            .withBirthDate(null)
            .withBirthYear(testYear)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));

        assertEquals(Year.of(2023), actual.events().subject().birthDate().get(0).value());
    }

    @Test
    void mapperMapsMonthYearBirthDateCorrectly() {
        var testYear = 2023;
        var testMonth = 12;
        var groJsonRecord = new GroJsonRecordBuilder()
            .withBirthDate(null)
            .withBirthYear(testYear)
            .withBirthMonth(testMonth)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));

        assertEquals(YearMonth.of(2023, 12), actual.events().subject().birthDate().get(0).value());
    }

    @Test
    void mapperMapsMonthOnlyBirthDateCorrectly() {
        var testMonth = 12;
        var groJsonRecord = new GroJsonRecordBuilder()
            .withBirthDate(null)
            .withBirthYear(null)
            .withBirthMonth(testMonth)
            .build();

        var actual = DeathNotificationSetMapper.generateDeathNotificationSet(new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID"));

        assertNull(actual.events().subject().birthDate().get(0).value());
    }
}
