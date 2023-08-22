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
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSetMapper;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private static final SQSMessage sqsMessage = new SQSMessage();
    private static final SQSEvent sqsEvent = new SQSEvent();
    private final DeathNotificationSet oldDeathNotificationSet = mock(DeathNotificationSet.class);

    @BeforeAll
    static void setup() {
        sqsMessage.setBody("Body Message");
        sqsEvent.setRecords(List.of(sqsMessage));
    }

    @BeforeEach
    void refreshSetup() throws JsonProcessingException {
        reset(awsService);
        reset(config);
        reset(objectMapper);

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
    void minimiseGroDeathEventDataReturnsMinimisedData() throws JsonProcessingException {
        var deathNotificationSet = mock(DeathNotificationSet.class);
        var enrichmentFields = List.of(EnrichmentField.DATE_OF_DEATH, EnrichmentField.POSTCODE);

        var deathNotificationSetMapper = mockStatic(DeathNotificationSetMapper.class);

        when(config.getEnrichmentFields()).thenReturn(enrichmentFields);
        deathNotificationSetMapper.when(
            () -> DeathNotificationSetMapper.generateMinimisedDeathNotificationSet(oldDeathNotificationSet, enrichmentFields)
        ).thenReturn(deathNotificationSet);

        var underTest = new DeathMinimisation(awsService, config, objectMapper);

        var result = underTest.handleRequest(sqsEvent, context);

        verify(objectMapper).readValue(sqsMessage.getBody(), DeathNotificationSet.class);
        deathNotificationSetMapper.verify(() ->
            DeathNotificationSetMapper.generateMinimisedDeathNotificationSet(oldDeathNotificationSet, enrichmentFields)
        );

        assertEquals(deathNotificationSet, result);
        deathNotificationSetMapper.close();
    }

    @Test
    void minimiseGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        reset(objectMapper);
        when(objectMapper.readValue(sqsMessage.getBody(), DeathNotificationSet.class))
            .thenThrow(mock(UnrecognizedPropertyException.class));

        var underTest = new DeathMinimisation(awsService, config, objectMapper);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(sqsEvent, context));

        assert(exception.getMessage().startsWith("Mock for UnrecognizedPropertyException"));
    }

}
