package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.GroSex;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroDeathNotificationMinimisationTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static GroDeathNotificationMinimisation underTest;
    private static final String enrichedDataJson = "{" +
        "\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"," +
        "\"sex\":\"FEMALE\"," +
        "\"dateOfBirth\":\"1972-02-20\"," +
        "\"dateOfDeath\":\"2021-12-31\"," +
        "\"registrationId\":\"123456789\"," +
        "\"eventTime\":\"2022-01-05T12:03:52\"," +
        "\"verificationLevel\":\"1\"," +
        "\"partialMonthOfDeath\":\"12\"," +
        "\"partialYearOfDeath\":\"2021\"," +
        "\"forenames\":\"Bob Burt\"," +
        "\"surname\":\"Smith\"," +
        "\"maidenSurname\":\"Jane\"," +
        "\"addressLine1\":\"888 Death House\"," +
        "\"addressLine2\":\"8 Death lane\"," +
        "\"addressLine3\":\"Deadington\"," +
        "\"addressLine4\":\"Deadshire\"," +
        "\"postcode\":\"XX1 1XX\"" +
        "}";

    private static final SQSMessage sqsMessage = new SQSMessage();
    private static final SQSEvent sqsEvent = new SQSEvent();

    @BeforeAll
    static void setup() {
        sqsMessage.setBody(enrichedDataJson);
        sqsEvent.setRecords(List.of(sqsMessage));
    }

    @BeforeEach
    void refreshSetup() throws JsonProcessingException {
        reset(awsService);
        reset(config);
        reset(objectMapper);

        when(objectMapper.readValue(sqsMessage.getBody(), GroDeathEventEnrichedData.class))
            .thenReturn(new GroDeathEventEnrichedData(
                "123a1234-a12b-12a1-a123-123456789012",
                GroSex.FEMALE,
                LocalDate.parse("1972-02-20"),
                LocalDate.parse("2021-12-31"),
                "123456789",
                LocalDateTime.parse("2022-01-05T12:03:52"),
                "1",
                "12",
                "2021",
                "Bob Burt",
                "Smith",
                "Jane",
                "888 Death House",
                "8 Death lane",
                "Deadington",
                "Deadshire",
                "XX1 1XX"
            ));
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        var mapper = mockConstruction(Mapper.class);
        new GroDeathNotificationMinimisation();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        assertEquals(1, mapper.constructed().size());
    }

    @Test
    void minimiseGroDeathEventDataReturnsMinimisedDataWithNoEnrichmentFields() throws JsonProcessingException {
        when(config.getEnrichmentFields()).thenReturn(List.of());

        underTest = new GroDeathNotificationMinimisation(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        verify(objectMapper).readValue(sqsMessage.getBody(), GroDeathEventEnrichedData.class);

        assertNull(result.eventDetails().sex());
        assertNull(result.eventDetails().dateOfBirth());
        assertNull(result.eventDetails().dateOfDeath());
        assertNull(result.eventDetails().registrationId());
        assertNull(result.eventDetails().eventTime());
        assertNull(result.eventDetails().verificationLevel());
        assertNull(result.eventDetails().partialMonthOfDeath());
        assertNull(result.eventDetails().partialYearOfDeath());
        assertNull(result.eventDetails().forenames());
        assertNull(result.eventDetails().surname());
        assertNull(result.eventDetails().maidenSurname());
        assertNull(result.eventDetails().addressLine1());
        assertNull(result.eventDetails().addressLine2());
        assertNull(result.eventDetails().addressLine3());
        assertNull(result.eventDetails().addressLine4());
    }

    @Test
    void minimiseGroDeathEventDataReturnsMinimisedDataWithPartialEnrichmentFields() throws JsonProcessingException {
        when(config.getEnrichmentFields()).thenReturn(List.of(
            EnrichmentField.SEX,
            EnrichmentField.DATE_OF_DEATH,
            EnrichmentField.FORENAMES,
            EnrichmentField.SURNAME,
            EnrichmentField.POSTCODE
        ));

        underTest = new GroDeathNotificationMinimisation(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        verify(objectMapper).readValue(sqsMessage.getBody(), GroDeathEventEnrichedData.class);

        assertEquals(GroSex.FEMALE, result.eventDetails().sex());
        assertNull(result.eventDetails().dateOfBirth());
        assertEquals(LocalDate.parse("2021-12-31"), result.eventDetails().dateOfDeath());
        assertNull(result.eventDetails().registrationId());
        assertNull(result.eventDetails().eventTime());
        assertNull(result.eventDetails().verificationLevel());
        assertNull(result.eventDetails().partialMonthOfDeath());
        assertNull(result.eventDetails().partialYearOfDeath());
        assertEquals("Bob Burt", result.eventDetails().forenames());
        assertEquals("Smith", result.eventDetails().surname());
        assertNull(result.eventDetails().maidenSurname());
        assertNull(result.eventDetails().addressLine1());
        assertNull(result.eventDetails().addressLine2());
        assertNull(result.eventDetails().addressLine3());
        assertNull(result.eventDetails().addressLine4());
    }

    @Test
    void minimiseGroDeathEventDataReturnsMinimisedDataWithAllEnrichmentFields() throws JsonProcessingException {
        when(config.getEnrichmentFields()).thenReturn(List.of(
            EnrichmentField.SEX,
            EnrichmentField.DATE_OF_BIRTH,
            EnrichmentField.DATE_OF_DEATH,
            EnrichmentField.REGISTRATION_ID,
            EnrichmentField.EVENT_TIME,
            EnrichmentField.VERIFICATION_LEVEL,
            EnrichmentField.PARTIAL_MONTH_OF_DEATH,
            EnrichmentField.PARTIAL_YEAR_OF_DEATH,
            EnrichmentField.FORENAMES,
            EnrichmentField.SURNAME,
            EnrichmentField.MAIDEN_SURNAME,
            EnrichmentField.ADDRESS_LINE_1,
            EnrichmentField.ADDRESS_LINE_2,
            EnrichmentField.ADDRESS_LINE_3,
            EnrichmentField.ADDRESS_LINE_4,
            EnrichmentField.POSTCODE
        ));

        underTest = new GroDeathNotificationMinimisation(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        verify(objectMapper).readValue(sqsMessage.getBody(), GroDeathEventEnrichedData.class);

        assertEquals(GroSex.FEMALE, result.eventDetails().sex());
        assertEquals(LocalDate.parse("1972-02-20"), result.eventDetails().dateOfBirth());
        assertEquals(LocalDate.parse("2021-12-31"), result.eventDetails().dateOfDeath());
        assertEquals("123456789", result.eventDetails().registrationId());
        assertEquals(LocalDateTime.parse("2022-01-05T12:03:52"), result.eventDetails().eventTime());
        assertEquals("1", result.eventDetails().verificationLevel());
        assertEquals("12", result.eventDetails().partialMonthOfDeath());
        assertEquals("2021", result.eventDetails().partialYearOfDeath());
        assertEquals("Bob Burt", result.eventDetails().forenames());
        assertEquals("Smith", result.eventDetails().surname());
        assertEquals("Jane", result.eventDetails().maidenSurname());
        assertEquals("888 Death House", result.eventDetails().addressLine1());
        assertEquals("8 Death lane", result.eventDetails().addressLine2());
        assertEquals("Deadington", result.eventDetails().addressLine3());
        assertEquals("Deadshire", result.eventDetails().addressLine4());
        assertEquals("XX1 1XX", result.eventDetails().postcode());
    }

    @Test
    void minimiseGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        reset(objectMapper);
        when(objectMapper.readValue(sqsMessage.getBody(), GroDeathEventEnrichedData.class))
            .thenThrow(mock(UnrecognizedPropertyException.class));

        var exception = assertThrows(RuntimeException.class, () -> underTest.handleRequest(sqsEvent, context));

        assert (exception.getMessage().startsWith("Mock for UnrecognizedPropertyException"));
    }
}
