package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.GuidScrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.approvaltests.scrubbers.Scrubbers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.deathnotification.DateWithDescription;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationBaseEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationSubject;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathRegistrationUpdateEvent;
import uk.gov.di.data.lep.library.dto.deathnotification.Name;
import uk.gov.di.data.lep.library.dto.deathnotification.NamePart;
import uk.gov.di.data.lep.library.dto.deathnotification.NamePartType;
import uk.gov.di.data.lep.library.dto.deathnotification.PostalAddress;
import uk.gov.di.data.lep.library.dto.deathnotification.Sex;
import uk.gov.di.data.lep.library.dto.deathnotification.StructuredDateTime;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConvertSetToOldFormatTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final DeathRegistrationSubject deathRegistrationSubject = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(
            new Name(
                null,
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JANE", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            ),
            new Name(
                "Name before marriage",
                List.of(
                    new NamePart(NamePartType.FAMILY_NAME, "JACKSON", null, null)
                ),
                null,
                null
            )
        ),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectAliasNameNoMaidenName = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(
            new Name(
                null,
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JANE", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            ),
            new Name(
                "Alias name",
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "ALIAS", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            )
        ),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectOnlyAliasName = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(
            new Name(
                "Alias name",
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "ALIAS", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            )
        ),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectEmptyNameDescription = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(
            new Name(
                "",
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JANE", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            )
        ),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectNoMaidenSurname = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(
            new Name(
                null,
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JANE", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            ),
            new Name(
                "Name before marriage",
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JACKIE", null, null)
                ),
                null,
                null
            )
        ),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectNoGivenName = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(
            new Name(
                null,
                List.of(
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            )),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectNoFamilyName = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(
            new Name(
                null,
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JANE", null, null)
                ),
                null,
                null
            )),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectNoNames = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, LocalDate.parse("1978-04-05"))),
        List.of(),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectYear = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, Year.of(1967))),
        List.of(
            new Name(
                null,
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JANE", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            )
        ),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationSubject deathRegistrationSubjectYearMonth = new DeathRegistrationSubject(
        List.of(new PostalAddress(
            "United Kingdom",
            null,
            "Carlton House",
            "10",
            null,
            null,
            null,
            null,
            null,
            "NE28 9FJ",
            "Lancaster Drive",
            null,
            null,
            null,
            null
        )),
        List.of(new DateWithDescription(null, YearMonth.of(1956, 5))),
        List.of(
            new Name(
                null,
                List.of(
                    new NamePart(NamePartType.GIVEN_NAME, "JANE", null, null),
                    new NamePart(NamePartType.FAMILY_NAME, "SMITH", null, null)
                ),
                null,
                null
            )
        ),
        List.of(Sex.FEMALE)
    );
    private static final DeathRegistrationEvent deathRegistrationEvent = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubject
    );
    private static final DeathRegistrationEvent deathRegistrationEventAliasNameNoMaidenName = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectAliasNameNoMaidenName
    );
    private static final DeathRegistrationEvent deathRegistrationEventAliasNameNoMaidenSurname = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectNoMaidenSurname
    );
    private static final DeathRegistrationEvent deathRegistrationEventNoGivenName = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectNoGivenName
    );
    private static final DeathRegistrationEvent deathRegistrationEventNoFamilyName = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectNoFamilyName
    );
    private static final DeathRegistrationEvent deathRegistrationEventOnlyAliasNameDescription = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectOnlyAliasName
    );
    private static final DeathRegistrationEvent deathRegistrationEventEmptyNameDescription = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectEmptyNameDescription
    );
    private static final DeathRegistrationEvent deathRegistrationEventNoNames = new DeathRegistrationEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectNoNames
    );
    private static final DeathRegistrationEvent deathRegistrationEventYear = new DeathRegistrationEvent(
        new DateWithDescription(null, Year.of(2020)),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectYear
    );
    private static final DeathRegistrationEvent deathRegistrationEventYearMonth = new DeathRegistrationEvent(
        new DateWithDescription(null, YearMonth.of(2020,3)),
        123456789,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-02-02T00:00:00")),
        deathRegistrationSubjectYearMonth
    );
    private static final DeathRegistrationUpdateEvent deathRegistrationUpdateEvent = new DeathRegistrationUpdateEvent(
        new DateWithDescription(null, LocalDate.parse("2020-06-06")),
        123456789,
        null,
        null,
        new StructuredDateTime(LocalDateTime.parse("2020-06-06T00:00:00")),
        deathRegistrationSubject
    );
    private static final DeathNotificationSet deathNotificationSet = new DeathNotificationSet(
        null,
        deathRegistrationEvent,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetAliasNameNoMaidenName = new DeathNotificationSet(
        null,
        deathRegistrationEventAliasNameNoMaidenName,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetAliasNameNoMaidenSurname = new DeathNotificationSet(
        null,
        deathRegistrationEventAliasNameNoMaidenSurname,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetNoGivenName = new DeathNotificationSet(
        null,
        deathRegistrationEventNoGivenName,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetNoFamilyName = new DeathNotificationSet(
        null,
        deathRegistrationEventNoFamilyName,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetOnlyAliasNameDescription = new DeathNotificationSet(
        null,
        deathRegistrationEventOnlyAliasNameDescription,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetEmptyNameDescription = new DeathNotificationSet(
        null,
        deathRegistrationEventEmptyNameDescription,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetNoNames = new DeathNotificationSet(
        null,
        deathRegistrationEventNoNames,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetWithYear = new DeathNotificationSet(
        null,
        deathRegistrationEventYear,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetWithYearMonth = new DeathNotificationSet(
        null,
        deathRegistrationEventYearMonth,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
    private static final DeathNotificationSet deathNotificationSetWithUpdateEvent = new DeathNotificationSet(
        null,
        deathRegistrationUpdateEvent,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

    private record DeathNotificationWithInvalidEvent(
        DateWithDescription deathDate,
        Integer deathRegistrationID,
        String freeFormatDeathDate,
        DeathRegistrationSubject subject
    ) implements DeathRegistrationBaseEvent {
    }

    private static final DeathNotificationSet deathNotificationSetWithInvalidEvent = new DeathNotificationSet(
        null,
        new DeathNotificationWithInvalidEvent(null, null, null, null),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

    @BeforeAll
    static void setup() {
        doNothing().when(awsService).putOnQueue(anyString());
        when(config.getTargetQueue()).thenReturn("targetQueue");
    }

    @BeforeEach
    void reset() {
        Mockito.reset(awsService);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new ConvertSetToOldFormat();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.close();
        }
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormat() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSet));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"JACKSON\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetWithUpdateEventToOldFormatConvertsDeathNotificationSetToOldFormat() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetWithUpdateEvent));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-06-06\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"JACKSON\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetWithInvalidEventThrowsMappingException() throws JsonProcessingException {
        var objectMapper = mock(ObjectMapper.class);
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("Message body");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(objectMapper.readValue("Message body", DeathNotificationSet.class)).thenReturn(deathNotificationSetWithInvalidEvent);

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(sqsEvent, context));

        assertInstanceOf(MappingException.class, exception);
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatWithAliasNames() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetAliasNameNoMaidenName));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatWithNoMaidenSurname() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetAliasNameNoMaidenSurname));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatNoGivenName() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetNoGivenName));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatNoFamilyName() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetNoFamilyName));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatWithNoNames() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetNoNames));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"\"," +
            "\"lastName\":\"\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatWithOnlyAliasName() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetOnlyAliasNameDescription));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"\"," +
            "\"lastName\":\"\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatWithEmptyNameDescription() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetEmptyNameDescription));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-06-06\"," +
            "\"dateOfBirth\":\"1978-04-05\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatWithYear() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetWithYear));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-01-01\"," +
            "\"dateOfBirth\":\"1967-01-01\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }
    @Test
    void convertSetToOldFormatConvertsDeathNotificationSetToOldFormatWithYearMonth() throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(objectMapper.writeValueAsString(deathNotificationSetWithYearMonth));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var expected =
            "\"type\":\"events\"," +
            "\"attributes\":{" +
            "\"eventType\":\"DEATH_NOTIFICATION\"," +
            "\"sourceId\":\"123456789\"," +
            "\"eventData\":{" +
            "\"registrationDate\":\"2020-02-02\"," +
            "\"firstNames\":\"JANE\"," +
            "\"lastName\":\"SMITH\"," +
            "\"sex\":\"FEMALE\"," +
            "\"dateOfDeath\":\"2020-03-01\"," +
            "\"dateOfBirth\":\"1956-05-01\"," +
            "\"birthPlace\":null," +
            "\"deathPlace\":null," +
            "\"maidenName\":\"\"," +
            "\"occupation\":null," +
            "\"retired\":null," +
            "\"address\":\"NE28 9FJ\"";

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains(expected));
    }

    @Test
    void failingToConvertSetToOldFormatThrowsException() throws JsonProcessingException {
        var mappingException = mock(JsonProcessingException.class);
        var objectMapper = mock(ObjectMapper.class);
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("Message body");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        when(objectMapper.readValue(anyString(), eq(DeathNotificationSet.class))).thenReturn(deathNotificationSet);
        when(objectMapper.writeValueAsString(any())).thenThrow(mappingException);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(sqsEvent, context));

        assertEquals(mappingException, exception.getCause());
    }

    private static Stream<Arguments> sourceSets() {
        return Stream.of(
            Arguments.of(deathNotificationSet, "deathNotificationSet"),
            Arguments.of(deathNotificationSetAliasNameNoMaidenName, "deathNotificationSetAliasNameNoMaidenName"),
            Arguments.of(deathNotificationSetAliasNameNoMaidenSurname, "deathNotificationSetAliasNameNoMaidenSurname"),
            Arguments.of(deathNotificationSetEmptyNameDescription, "deathNotificationSetEmptyNameDescription"),
            Arguments.of(deathNotificationSetNoFamilyName, "deathNotificationSetNoFamilyName"),
            Arguments.of(deathNotificationSetNoGivenName, "deathNotificationSetNoGivenName"),
            Arguments.of(deathNotificationSetNoNames, "deathNotificationSetNoNames"),
            Arguments.of(deathNotificationSetOnlyAliasNameDescription, "deathNotificationSetOnlyAliasNameDescription"),
            Arguments.of(deathNotificationSetWithUpdateEvent, "deathNotificationSetWithUpdateEvent"),
            Arguments.of(deathNotificationSetWithYear, "deathNotificationSetWithYear"),
            Arguments.of(deathNotificationSetWithYearMonth, "deathNotificationSetWithYearMonth")
        );
    }

    @ParameterizedTest
    @MethodSource("sourceSets")
    void convertSetApprovalTest(DeathNotificationSet sourceSet, String name) throws JsonProcessingException {
        var objectMapper = Mapper.objectMapper();
        var sqsMessage = new SQSMessage();

        sqsMessage.setBody(objectMapper.writeValueAsString(sourceSet));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        var underTest = new ConvertSetToOldFormat(awsService, config, objectMapper);

        when(config.getTargetTopic()).thenReturn("Target Topic");

        underTest.handleRequest(sqsEvent, null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(awsService).putOnTopic(captor.capture());

        var options = new Options(Scrubbers.scrubAll(
            new GuidScrubber(),
            new RegExScrubber("\"iat\":\\d+,", n -> "\"iat\":" + n + ","))
        );
        Approvals.verify(captor.getValue(), Approvals.NAMES.withParameters(options, name));
    }
}
