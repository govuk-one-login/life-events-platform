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

        var event = new APIGatewayProxyRequestEvent().withBody("{'sourceId':'123a1234-a12b-12a1-a123-123456789012'}");

        var result = underTest.handleRequest(event, context);

        assertEquals("123a1234-a12b-12a1-a123-123456789012", result.sourceId());
    }

    @Test
    void validateGroDeathEventDataFailsIfNoSourceId() {

        var event = new APIGatewayProxyRequestEvent().withBody("{'notSourceId':'123a1234-a12b-12a1-a123-123456789012'}");

        var exception = assertThrows(IllegalArgumentException.class, () -> underTest.handleRequest(event, context));

        assertEquals("sourceId cannot be null", exception.getMessage());
    }
}
