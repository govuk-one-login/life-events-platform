package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;
import uk.gov.di.data.lep.library.exceptions.MappingException;
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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class GroDeathEnrichmentTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final GroDeathEnrichment underTest = new GroDeathEnrichment(awsService, config, objectMapper);

    @BeforeEach
    void refreshSetup() {
        reset(objectMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        var mapper = mockStatic(Mapper.class);
        new GroDeathEnrichment();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        mapper.verify(Mapper::objectMapper, times(1));
        mapper.close();
    }

    @Test
    void enrichGroDeathEventDataReturnsEnrichedData() throws JsonProcessingException {
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("A message body");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        when(objectMapper.readValue(sqsMessage.getBody(), GroJsonRecord.class))
            .thenReturn(new GroJsonRecordBuilder().build());

        var result = underTest.handleRequest(sqsEvent, context);

        assertEquals(1234567890, result.sourceId());
        assertEquals(GenderAtRegistration.FEMALE, result.sex());
        assertEquals(LocalDate.parse("1967-03-06"), result.dateOfBirth());
        assertEquals(LocalDate.parse("2007-03-06"), result.dateOfDeath());
        assertEquals(1234567890, result.registrationId());
        assertEquals(LocalDateTime.parse("2023-03-06T09:30:50"), result.eventTime());
        assertNull(result.partialMonthOfDeath());
        assertNull(result.partialYearOfDeath());
        assertEquals(List.of("ERICA"), result.forenames());
        assertEquals("BLOGG", result.surname());
        assertNull(result.maidenSurname());
        assertNull(result.flat());
        assertNull(result.building());
        assertEquals(List.of("123 Street"), result.lines());
        assertEquals("GT8 5HG", result.postcode());
    }

    @Test
    void enrichGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("A message body");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        when(objectMapper.readValue(sqsMessage.getBody(), GroJsonRecord.class))
            .thenThrow(mock(UnrecognizedPropertyException.class));

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(sqsEvent, context));

        assert (exception.getMessage().startsWith("Mock for UnrecognizedPropertyException"));
    }
}
