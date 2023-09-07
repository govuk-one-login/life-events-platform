package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.GuidScrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.approvaltests.scrubbers.Scrubbers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeathEnrichmentIntegrationTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final DeathEnrichment underTest = new DeathEnrichment(awsService, config, Mapper.objectMapper());

    @Test
    void approvalTestForOutput() throws JsonProcessingException {
        var groJsonRecord = new GroJsonRecordBuilder().build();
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody(Mapper.objectMapper().writeValueAsString(groJsonRecord));
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        when(config.getTargetTopic()).thenReturn("Target Topic");

        underTest.handleRequest(sqsEvent, context);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(awsService).putOnTopic(captor.capture());

        var options = new Options(Scrubbers.scrubAll(
            new GuidScrubber(),
            new RegExScrubber("\"iat\":\\d+,", n -> "\"iat\":" + n + ","))
        );
        Approvals.verify(captor.getValue(), options);
    }
}
