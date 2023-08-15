package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class GroDeathValidationTest {
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final AwsService awsService = mock(AwsService.class);
    private static final GroDeathValidation underTest = new GroDeathValidation(awsService, config, objectMapper);

    private final GroJsonRecord record = new GroJsonRecordBuilder().build();

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
        clearInvocations(config);
        clearInvocations(objectMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        var mapper = mockStatic(Mapper.class);
        new GroDeathValidation();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        mapper.verify(Mapper::objectMapper, times(1));
        mapper.close();
    }

    @Test
    void validateGroDeathEventDataReturnsAPIGatewayProxyResponseEventWithStatusCode201() throws JsonProcessingException {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");

        when(objectMapper.readValue(event.getBody(), GroJsonRecord.class)).thenReturn(record);

        var result = underTest.handleRequest(event, context);

        assertEquals(201, result.getStatusCode());
    }

    @Test
    void validateGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"notSourceId\":\"an id but not a source id\"}");

        when(objectMapper.readValue(event.getBody(), GroJsonRecord.class)).thenThrow(mock(UnrecognizedPropertyException.class));

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        assert(exception.getMessage().startsWith("Mock for UnrecognizedPropertyException"));
    }
}
