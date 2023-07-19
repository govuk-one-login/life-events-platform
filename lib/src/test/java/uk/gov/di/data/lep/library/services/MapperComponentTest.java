package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapperComponentTest {
//    @Test
//    void mapperComponentThrowsErrorIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
//        var event = new APIGatewayProxyRequestEvent().withBody("{\"notSourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");
//
//        var exception = assertThrows(RuntimeException.class, () -> underTest.handleRequest(event, context));
//
//        assertEquals("UnrecognisedProperties test exception", (exception.getCause()).getMessage());
//    }
}
