package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.GuidScrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.approvaltests.scrubbers.Scrubbers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAuditExtensions;
import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeathValidationTest {
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final AwsService awsService = mock(AwsService.class);
    private static final DeathValidation underTest = new DeathValidation(awsService, config, objectMapper);

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
        new DeathValidation();
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

        var result = underTest.handleRequest(event, context);

        assertEquals(400, result.getStatusCode());
    }

    @Test
    void validateGroDeathEventDataThrowsExceptionIfNeitherLockedNorUpdateTimeAreGiven() throws JsonProcessingException {
        var event = new APIGatewayProxyRequestEvent().withBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");
        var groJsonRecord = new GroJsonRecordBuilder()
            .withLockedDateTime(null)
            .withUpdateDateTime(null)
            .withUpdateReason(null)
            .build();

        when(objectMapper.readValue(event.getBody(), GroJsonRecord.class)).thenReturn(groJsonRecord);

        var result = underTest.handleRequest(event, context);

        assertEquals(400, result.getStatusCode());
    }

    @Test
    void deathValidationSnapshotTest() throws JsonProcessingException {
        var groJsonRecord = new GroJsonRecordBuilder().build();
        var event = new APIGatewayProxyRequestEvent()
            .withBody(Mapper.objectMapper().writeValueAsString(groJsonRecord))
            .withHeaders(Map.of("CorrelationID", "correlationID"));

        when(config.getTargetTopic()).thenReturn("Target Topic");

        var snapshotUnderTest = new DeathValidation(awsService, config, Mapper.objectMapper());

        snapshotUnderTest.handleRequest(event, null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(awsService).putOnTopic(captor.capture());

        var options = new Options(Scrubbers.scrubAll(
            new GuidScrubber(),
            new RegExScrubber("\"iat\":\\d+,", n -> "\"iat\":" + n + ","))
        );
        Approvals.verify(captor.getValue(), options);
    }

    @Test
    void validateGroDeathEventAuditsData() throws JsonProcessingException {
        var event = new APIGatewayProxyRequestEvent()
            .withBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}")
            .withHeaders(Map.of("CorrelationID", "correlationID"));

        when(objectMapper.readValue(event.getBody(), GroJsonRecord.class)).thenReturn(record);

        var deathValidationAudit = new DeathValidationAudit(new DeathValidationAuditExtensions("correlationID"));
        when(objectMapper.writeValueAsString(deathValidationAudit)).thenReturn("Audit data");

        underTest.handleRequest(event, context);

        verify(objectMapper).writeValueAsString(deathValidationAudit);
        verify(awsService).putOnAuditQueue("Audit data");
    }
}
