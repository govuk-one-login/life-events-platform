package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeathMinimisationTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final ObjectWriter writer = mock(ObjectWriter.class);
    private static final String sqsMessageBody =
        "{\"events\":" +
            "{" +
            "\"https://ssf.account.gov.uk/v1/deathRegistrationUpdate\":{" +
            "\"deathRegistrationID\":123456," +
            "\"deathDate\":{\"value\":\"2011-11-29\"}," +
            "\"recordUpdateTime\":{\"value\":\"2022-11-02T12:00:00\"}," +
            "\"subject\":{" +
            "\"address\":[{" +
            "\"buildingNumber\":\"10\"," +
            "\"streetName\":\"Alesham Avenue\"," +
            "\"postalCode\":\"OX33 1DF\"" +
            "}]," +
            "\"birthDate\":[{\"value\":\"1954-11-13\"}]," +
            "\"name\":[{\"nameParts\":[{\"type\":\"GivenName\", \"value\":\"JEREMY\"}]}]," +
            "\"sex\":[\"Male\"]" +
            "}}}}";
    private static final SQSMessage sqsMessage = new SQSMessage();
    private static final SQSEvent sqsEvent = new SQSEvent();
    private final DeathNotificationSet oldDeathNotificationSet = mock(DeathNotificationSet.class);

    @BeforeAll
    static void setup() {
        sqsMessage.setBody(sqsMessageBody);
        sqsEvent.setRecords(List.of(sqsMessage));
    }

    @BeforeEach
    void refreshSetup() throws JsonProcessingException {
        reset(awsService);
        reset(config);
        reset(objectMapper);
        reset(writer);

        when(objectMapper.readValue(sqsMessage.getBody(), DeathNotificationSet.class))
            .thenReturn(oldDeathNotificationSet);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new DeathMinimisation();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.close();
        }
    }

    @Test
    void minimiseEnrichedDataReturnsMinimisedDataAsString() {
        when(config.getEnrichmentFields()).thenReturn(List.of(EnrichmentField.ADDRESS, EnrichmentField.NAME));

        var underTest = new DeathMinimisation(awsService, config, Mapper.objectMapper());

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains("address"));
        assertTrue(result.contains("name"));
        assertFalse(result.contains("birthDate"));
        assertFalse(result.contains("deathDate"));
        assertFalse(result.contains("freeFormatDeathDate"));
        assertFalse(result.contains("sex"));
    }

    @Test
    void minimiseEnrichedDataReturnsMinimisedDataAsStringAllFields() {
        when(config.getEnrichmentFields()).thenReturn(List.of(
            EnrichmentField.ADDRESS,
            EnrichmentField.BIRTH_DATE,
            EnrichmentField.DEATH_DATE,
            EnrichmentField.NAME,
            EnrichmentField.SEX
        ));

        var underTest = new DeathMinimisation(awsService, config, Mapper.objectMapper());

        var result = underTest.handleRequest(sqsEvent, context);

        assertTrue(result.contains("address"));
        assertTrue(result.contains("birthDate"));
        assertTrue(result.contains("deathDate"));
        assertTrue(result.contains("freeFormatDeathDate"));
        assertTrue(result.contains("name"));
        assertTrue(result.contains("sex"));
    }
    @Test
    void minimiseEnrichedDataReturnsMinimisedDataAsStringNoFields() {
        when(config.getEnrichmentFields()).thenReturn(List.of());

        var underTest = new DeathMinimisation(awsService, config, Mapper.objectMapper());

        var result = underTest.handleRequest(sqsEvent, context);

        assertFalse(result.contains("address"));
        assertFalse(result.contains("birthDate"));
        assertFalse(result.contains("deathDate"));
        assertFalse(result.contains("freeFormatDeathDate"));
        assertFalse(result.contains("name"));
        assertFalse(result.contains("sex"));
    }

    @Test
    void minimiseEnrichedDataReturnsMinimisedData() throws JsonProcessingException {
        when(objectMapper.writer(any(SimpleFilterProvider.class))).thenReturn(writer);
        when(writer.writeValueAsString(any())).thenReturn("Minimised death notification set");

        var underTest = new DeathMinimisation(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        verify(objectMapper).readValue(sqsMessage.getBody(), DeathNotificationSet.class);

        assertEquals("Minimised death notification set", result);
    }

    @Test
    void minimiseEnrichedDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        reset(objectMapper);
        when(objectMapper.readValue(sqsMessage.getBody(), DeathNotificationSet.class))
            .thenThrow(UnrecognizedPropertyException.class);

        var underTest = new DeathMinimisation(awsService, config, objectMapper);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(sqsEvent, context));

        assertThat(exception.getCause()).isInstanceOf(UnrecognizedPropertyException.class);
    }
}
