package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroDeathValidationTest {
    @Mock
    private static Context context = mock(Context.class);
    @Mock
    private static LambdaLogger logger = mock(LambdaLogger.class);
    private static GroDeathValidation underTest;

    @BeforeAll
    static void setup() {
        underTest = mock(GroDeathValidation.class);
        when(underTest.publish(any())).thenReturn(new GroDeathEventBaseData(("")));
        when(underTest.handleRequest(any(), any())).thenCallRealMethod();
        when(context.getLogger()).thenReturn(logger);
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(underTest);
        clearInvocations(logger);
    }

    @Test
    void validateGroDeathEventDataReturnsAPIGatewayProxyResponseEventWithStatusCode201() {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");

        var result = underTest.handleRequest(event, context);

        verify(logger).log("Validating request");

        assertEquals(201, result.getStatusCode());
    }

    @Test
    void validateGroDeathEventDataPublishesBaseData() {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");

        underTest.handleRequest(event, context);

        verify(logger).log("Validating request");

        verify(underTest).publish(argThat(x -> x.sourceId().equals("123a1234-a12b-12a1-a123-123456789012")));
    }

    @Test
    void validateGroDeathEventDataFailsIfNoSourceId() {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":null}");

        var exception = assertThrows(IllegalArgumentException.class, () -> underTest.handleRequest(event, context));

        verify(logger).log("Validating request");

        assertEquals("sourceId cannot be null", exception.getMessage());
    }

    @Test
    void validateGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"notSourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");

        var exception = assertThrows(RuntimeException.class, () -> underTest.handleRequest(event, context));

        verify(logger).log("Validating request");

        assertEquals(
            "Unrecognized field \"notSourceId\" (class uk.gov.di.data.lep.dto.GroDeathEvent), not marked as ignorable",
            ((UnrecognizedPropertyException) exception.getCause()).getOriginalMessage()
        );
    }
}
