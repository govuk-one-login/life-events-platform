package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.GuidScrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.approvaltests.scrubbers.Scrubbers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithCorrelationID;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeathEnrichmentIntegrationTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final DeathEnrichment underTest = new DeathEnrichment(awsService, config, Mapper.objectMapper());

    @BeforeEach
    void reset() {
        Mockito.reset(awsService);
    }

    @Test
    void approvalTestForOutput() throws JsonProcessingException {
        var groJsonRecord = new GroJsonRecordBuilder().build();
        var groJsonRecordWithCorrelationID = new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID");
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(Mapper.objectMapper().writeValueAsString(groJsonRecordWithCorrelationID));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        when(config.getTargetTopic()).thenReturn("Target Topic");

        underTest.handleRequest(sqsEvent, null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(awsService).putOnTopic(captor.capture());

        var options = new Options(Scrubbers.scrubAll(
            new GuidScrubber(),
            new RegExScrubber("\"iat\":\\d+,", n -> "\"iat\":" + n + ","))
        );
        Approvals.verify(captor.getValue(), options);
    }

    @Test
    void approvalTestForUpdate() throws JsonProcessingException {
        var groJsonRecord = new GroJsonRecordBuilder()
            .withUpdateReason(1)
            .withUpdateDateTime(LocalDateTime.of(2019, Month.APRIL, 3, 10, 1))
            .withLockedDateTime(null)
            .build();
        var groJsonRecordWithCorrelationID = new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID");
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(Mapper.objectMapper().writeValueAsString(groJsonRecordWithCorrelationID));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        when(config.getTargetTopic()).thenReturn("Target Topic");

        underTest.handleRequest(sqsEvent, null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(awsService).putOnTopic(captor.capture());

        var options = new Options(Scrubbers.scrubAll(
            new GuidScrubber(),
            new RegExScrubber("\"iat\":\\d+,", n -> "\"iat\":" + n + ","))
        );
        Approvals.verify(captor.getValue(), options);
    }
}
