package uk.gov.di.data.lep.library.dto.deathnotification;

import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;
import uk.gov.di.data.lep.library.dto.gro.GroPersonNameStructure;

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

    @Tracing
    public static DeathNotificationSet generateDeathNotificationSet(GroJsonRecord groRecord) {
        var iat = Instant.now().getEpochSecond();
        var jti = UUID.randomUUID().toString();
        var toe = groRecord.recordLockedDateTime() == null
            ? groRecord.recordUpdateDateTime().toEpochSecond(ZoneOffset.UTC)
            : groRecord.recordLockedDateTime().toEpochSecond(ZoneOffset.UTC);
        var txn = UUID.randomUUID().toString();

        return new DeathNotificationSet(
            null,
            generateDeathRegistrationBaseEvent(groRecord),
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

    @Tracing
    private static DeathRegistrationBaseEvent generateDeathRegistrationBaseEvent(GroJsonRecord groJsonRecord) {
        var dateOfDeath = generateDate(
            (groJsonRecord.deceasedDeathDate() == null ? null : groJsonRecord.deceasedDeathDate().personDeathDate()),
            groJsonRecord.partialYearOfDeath(),
            groJsonRecord.partialMonthOfDeath()
        );
        var deathDate = new DateWithDescription(groJsonRecord.qualifierText(), dateOfDeath);
        return groJsonRecord.recordLockedDateTime() == null
            ? generateDeathRegistrationUpdateEvent(groJsonRecord, deathDate)
            : generateDeathRegistrationEvent(groJsonRecord, deathDate);
    }

    @Tracing
    private static DeathRegistrationEvent generateDeathRegistrationEvent(GroJsonRecord groJsonRecord, DateWithDescription deathDate) {
        return new DeathRegistrationEvent(
            deathDate,
            groJsonRecord.registrationID(),
            groJsonRecord.freeFormatDeathDate(),
            new StructuredDateTime(groJsonRecord.recordLockedDateTime()),
            generateDeathRegistrationSubject(groJsonRecord)
        );
    }

    @Tracing
    private static DeathRegistrationUpdateEvent generateDeathRegistrationUpdateEvent(GroJsonRecord groJsonRecord, DateWithDescription deathDate) {
        return new DeathRegistrationUpdateEvent(
            deathDate,
            groJsonRecord.registrationID(),
            DeathRegistrationUpdateReasonType.fromGroRegistrationType(groJsonRecord.recordUpdateReason()),
            groJsonRecord.freeFormatDeathDate(),
            new StructuredDateTime(groJsonRecord.recordUpdateDateTime()),
            generateDeathRegistrationSubject(groJsonRecord)
        );
    }

    @Tracing
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

        var birthDate = new DateWithDescription(null, dateOfBirth);
        var names = generateNames(groJsonRecord);
        var sex = Sex.fromGro(groJsonRecord.deceasedGender());

        return new DeathRegistrationSubject(
            List.of(address),
            List.of(birthDate),
            names,
            List.of(sex),
            groJsonRecord.freeFormatBirthDate()
        );
    }

    @Tracing
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

    @Tracing
    private static List<Name> generateNames(GroJsonRecord groJsonRecord) {
        var groName = groJsonRecord.deceasedName();
        var groAliasNames = groJsonRecord.deceasedAliasNames();
        var groMaidenName = groJsonRecord.deceasedMaidenName();

        var names = Stream.of(generateName(groName, null));

        if (groAliasNames != null && !groAliasNames.isEmpty()) {
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

    @Tracing
    private static String getAliasNameTypeOrNull(List<String> deceasedAliasNameTypes, Integer index) {
        return deceasedAliasNameTypes.size() > index
            ? deceasedAliasNameTypes.get(index)
            : null;
    }

    @Tracing
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
