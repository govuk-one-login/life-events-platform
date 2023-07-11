package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroDeathValidationTest {
    @Mock
    private Context context;
    private final GroDeathValidation underTest = new GroDeathValidation();

    @Test
    void validateGroDeathEventDataReturnsBaseData() {

        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");

        var result = underTest.handleRequest(event, context);

        assertEquals("123a1234-a12b-12a1-a123-123456789012", result.sourceId());
    }

    @Test
    void validateGroDeathEventDataFailsIfNoSourceId() {

        var event = new APIGatewayProxyRequestEvent().withBody("{\"notSourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");

        var exception = assertThrows(RuntimeException.class, () -> underTest.handleRequest(event, context));

        assertEquals("com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field \"notSourceId\" (class uk.gov.di.data.lep.dto.GroDeathEvent), not marked as ignorable (one known property: \"sourceId\"])\n" +
            " at [Source: (String)\"{\"notSourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}\"; line: 1, column: 55] (through reference chain: uk.gov.di.data.lep.dto.GroDeathEvent[\"notSourceId\"])", exception.getMessage());
    }
}
