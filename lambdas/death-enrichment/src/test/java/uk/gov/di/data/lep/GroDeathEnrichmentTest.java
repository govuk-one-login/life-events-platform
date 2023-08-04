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
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;
import uk.gov.di.data.lep.library.enums.GroSex;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.reset;
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
        var mapper = mockConstruction(Mapper.class);
        new GroDeathEnrichment();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        assertEquals(1, mapper.constructed().size());
    }

    @Test
    void enrichGroDeathEventDataReturnsEnrichedData() throws JsonProcessingException {
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        when(objectMapper.readValue(sqsMessage.getBody(), GroDeathEventBaseData.class))
            .thenReturn(new GroDeathEventBaseData(
                "123a1234-a12b-12a1-a123-123456789012"
            ));

        var result = underTest.handleRequest(sqsEvent, context);

        assertEquals("123a1234-a12b-12a1-a123-123456789012", result.sourceId());
        assertEquals(GroSex.FEMALE, result.sex());
        assertEquals(LocalDate.parse("1972-02-20"), result.dateOfBirth());
        assertEquals(LocalDate.parse("2021-12-31"), result.dateOfDeath());
        assertEquals("123456789", result.registrationId());
        assertEquals(LocalDateTime.parse("2022-01-05T12:03:52"), result.eventTime());
        assertEquals("1", result.verificationLevel());
        assertEquals("12", result.partialMonthOfDeath());
        assertEquals("2021", result.partialYearOfDeath());
        assertEquals("Bob Burt", result.forenames());
        assertEquals("Smith", result.surname());
        assertEquals("Jane", result.maidenSurname());
        assertEquals("888 Death House", result.addressLine1());
        assertEquals("8 Death lane", result.addressLine2());
        assertEquals("Deadington", result.addressLine3());
        assertEquals("Deadshire", result.addressLine4());
        assertEquals("XX1 1XX", result.postcode());
    }

    @Test
    void enrichGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        when(objectMapper.readValue(sqsMessage.getBody(), GroDeathEventBaseData.class))
            .thenThrow(mock(UnrecognizedPropertyException.class));

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(sqsEvent, context));

        assert (exception.getMessage().startsWith("Mock for UnrecognizedPropertyException"));
    }
}
