package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.di.data.lep.dto.GroDeathEvent;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroDeathValidationTest {
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final LambdaLogger logger = mock(LambdaLogger.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static MockedStatic<AwsService> awsService;
    private static final GroDeathValidation underTest = new GroDeathValidation(config, objectMapper);

    @BeforeAll
    static void setup() {
        awsService = mockStatic(AwsService.class);
        when(context.getLogger()).thenReturn(logger);
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(logger);
        clearInvocations(config);
        clearInvocations(objectMapper);
    }

    @AfterAll
    public static void tearDown() {
        awsService.close();
    }

    @Test
    void validateGroDeathEventDataReturnsAPIGatewayProxyResponseEventWithStatusCode201() throws JsonProcessingException {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");

        when(objectMapper.readValue(event.getBody(), GroDeathEvent.class)).thenReturn(new GroDeathEvent("123a1234-a12b-12a1-a123-123456789012"));

        var result = underTest.handleRequest(event, context);

        verify(logger).log("Validating request");

        assertEquals(201, result.getStatusCode());
    }

    @Test
    void validateGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"notSourceId\":\"an id but not a source id\"}");

        when(objectMapper.readValue(event.getBody(), GroDeathEvent.class)).thenThrow(mock(UnrecognizedPropertyException.class));

        var exception = assertThrows(RuntimeException.class, () -> underTest.handleRequest(event, context));

        verify(logger).log("Validating request");
        verify(logger).log("Failed to validate request");

        assert(exception.getMessage().startsWith("Mock for UnrecognizedPropertyException"));

    }

    @Test
    void validateGroDeathEventDataFailsIfNullSourceId() throws JsonProcessingException {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":null}");

        when(objectMapper.readValue(event.getBody(), GroDeathEvent.class)).thenReturn(new GroDeathEvent(null));

        var exception = assertThrows(IllegalArgumentException.class, () -> underTest.handleRequest(event, context));

        verify(logger).log("Validating request");

        assertEquals("sourceId cannot be null", exception.getMessage());
    }
}
