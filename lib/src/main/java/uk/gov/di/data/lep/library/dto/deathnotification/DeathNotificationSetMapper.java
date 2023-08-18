package uk.gov.di.data.lep.library.dto.deathnotification;

import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.dto.GroPersonNameStructure;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DeathNotificationSetMapper {
    private DeathNotificationSetMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static DeathNotificationSet generateDeathNotificationSet(GroJsonRecord groRecord) {
        var newEvent = generateDeathRegistrationEvent(groRecord);

        var isUpdate = groRecord.recordLockedDateTime() == null;
        var events = new DeathRegistrationEventMapping(
            isUpdate ? null : newEvent,
            isUpdate ? newEvent : null
        );
        var iat = Instant.now().getEpochSecond();
        var jti = UUID.randomUUID().toString();
        var toe = isUpdate
            ? groRecord.recordUpdateDateTime().toEpochSecond(ZoneOffset.UTC)
            : groRecord.recordLockedDateTime().toEpochSecond(ZoneOffset.UTC);
        var txn = UUID.randomUUID().toString();

        return new DeathNotificationSet(
            null,
            events,
            null,
            iat,
            null,
            jti,
            null,
            null,
            toe,
            txn
        );
    }

    private static DeathRegistrationEvent generateDeathRegistrationEvent(GroJsonRecord groJsonRecord) {
        var dateOfDeath = generateDate(
            groJsonRecord.deceasedDeathDate() == null ? null : groJsonRecord.deceasedDeathDate().personDeathDate(),
            groJsonRecord.partialYearOfDeath(),
            groJsonRecord.partialMonthOfDeath()
        );

        var deathDate = new IsoDate(groJsonRecord.qualifierText(), dateOfDeath);
        var deathRegistrationID = groJsonRecord.registrationID();
        var deathRegistrationUpdateReason = groJsonRecord.recordUpdateReason() != null
            ? DeathRegistrationUpdateReasonType.fromGroRegistrationType(groJsonRecord.recordUpdateReason())
            : null;
        var freeFormatDeathDate = groJsonRecord.freeFormatDeathDate();
        var recordUpdateTime = groJsonRecord.recordUpdateDateTime() != null
            ? new StructuredDateTime(groJsonRecord.recordUpdateDateTime())
            : null;
        var deathRegistrationTime = groJsonRecord.recordLockedDateTime() != null
            ? new StructuredDateTime(groJsonRecord.recordLockedDateTime())
            : null;

        return new DeathRegistrationEvent(
            deathDate,
            deathRegistrationID,
            deathRegistrationUpdateReason,
            freeFormatDeathDate,
            recordUpdateTime,
            deathRegistrationTime,
            generateDeathRegistrationSubject(groJsonRecord)
        );
    }

    private static DeathRegistrationSubject generateDeathRegistrationSubject(GroJsonRecord groJsonRecord) {
        var address = new PostalAddress(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            groJsonRecord.deceasedAddress().postcode(),
            null,
            null,
            null,
            null,
            null
        );
        var dateOfBirth = generateDate(
            groJsonRecord.deceasedBirthDate() == null ? null : groJsonRecord.deceasedBirthDate().personBirthDate(),
            groJsonRecord.partialYearOfBirth(),
            groJsonRecord.partialMonthOfBirth()
        );

        var birthDate = new IsoDate(null, dateOfBirth);
        var names = generateNames(groJsonRecord);
        var sex = Sex.fromGro(groJsonRecord.deceasedGender());

        return new DeathRegistrationSubject(
            List.of(address),
            List.of(birthDate),
            names,
            List.of(sex)
        );
    }

    private static TemporalAccessor generateDate(LocalDate localDate, Integer year, Integer month) {
        if (localDate != null) {
            return localDate;
        }
        if (month != null && year != null) {
            return YearMonth.of(year, month);
        }
        if (year != null) {
            return Year.of(year);
        }
        return null;
    }

    private static List<Name> generateNames(GroJsonRecord groJsonRecord) {
        var groName = groJsonRecord.deceasedName();
        var groAliasNames = groJsonRecord.deceasedAliasNames();
        var groMaidenName = groJsonRecord.deceasedMaidenName();

        var names = Stream.of(generateName(groName, null));

        if (groAliasNames != null) {
            var aliasNames = IntStream.range(0, groAliasNames.size())
                .mapToObj(i -> generateName(
                    groAliasNames.get(i),
                    getAliasNameTypeOrNull(groJsonRecord.deceasedAliasNameTypes(), i)
                ));
            names = Stream.concat(names, aliasNames);
        }

        if (groMaidenName != null) {
            var maidenName = new Name(
                "Name before marriage",
                Stream.concat(
                    groName.personGivenNames().stream().map(n -> new NamePart(NamePartType.GIVEN_NAME, n)),
                    Stream.of(new NamePart(NamePartType.FAMILY_NAME, groMaidenName))).toList()
            );
            names = Stream.concat(names, Stream.of(maidenName));
        }

        return names.toList();
    }

    private static String getAliasNameTypeOrNull(List<String> deceasedAliasNameTypes, Integer index) {
        return deceasedAliasNameTypes.size() > index
            ? deceasedAliasNameTypes.get(index)
            : null;
    }

    private static Name generateName(GroPersonNameStructure nameStructure, String description) {
        var givenNameParts = nameStructure.personGivenNames().stream().map(n ->
            new NamePart(NamePartType.GIVEN_NAME, n)
        );
        return new Name(
            description,
            Stream.concat(givenNameParts, Stream.of(new NamePart(NamePartType.FAMILY_NAME, nameStructure.personFamilyName()))).toList()
        );
    }
}
