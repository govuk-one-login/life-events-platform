package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithCorrelationID;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSetMapper;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathEnrichmentAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathEnrichmentAuditExtensions;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeathEnrichmentTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final Context context = mock(Context.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final DeathEnrichment underTest = new DeathEnrichment(awsService, config, objectMapper);


    @BeforeAll
    static void setup(){
        when(config.getTargetTopic()).thenReturn("Target Topic");
    }

    @BeforeEach
    void refreshSetup() {
        reset(objectMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new DeathEnrichment();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.close();
        }
    }

    @Test
    void enrichGroDeathEventDataReturnsEnrichedData() throws JsonProcessingException {
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("A message body");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var groJsonRecord = new GroJsonRecordBuilder().build();
        var groJsonRecordWithCorrelationID = new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID");
        var deathNotificationSet = mock(DeathNotificationSet.class);

        when(objectMapper.readValue(sqsMessage.getBody(), GroJsonRecordWithCorrelationID.class)).thenReturn(groJsonRecordWithCorrelationID);
        when(objectMapper.writeValueAsString(deathNotificationSet)).thenReturn("Death notification set");
        var deathNotificationSetMapper = mockStatic(DeathNotificationSetMapper.class);
        deathNotificationSetMapper.when(() -> DeathNotificationSetMapper.generateDeathNotificationSet(groJsonRecordWithCorrelationID))
            .thenReturn(deathNotificationSet);

        underTest.handleRequest(sqsEvent, context);

        verify(objectMapper).writeValueAsString(deathNotificationSet);
        verify(awsService).putOnTopic("Death notification set");

        deathNotificationSetMapper.close();
    }

    @Test
    void enrichGroDeathEventDataFailsIfBodyHasUnrecognisedProperties() throws JsonProcessingException {
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("A message body");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        when(objectMapper.readValue(sqsMessage.getBody(), GroJsonRecordWithCorrelationID.class))
            .thenThrow(UnrecognizedPropertyException.class);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(sqsEvent, context));

        assertThat(exception.getCause()).isInstanceOf(UnrecognizedPropertyException.class);
    }

    @Test
    void enrichGroDeathEventAuditsData() throws JsonProcessingException {
        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("A message body");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        var groJsonRecord = new GroJsonRecordBuilder().build();
        var groJsonRecordWithCorrelationID = new GroJsonRecordWithCorrelationID(groJsonRecord, "correlationID");
        var deathNotificationSet = mock(DeathNotificationSet.class);
        when(deathNotificationSet.txn()).thenReturn("correlationID");
        var deathEnrichmentAudit = new DeathEnrichmentAudit(new DeathEnrichmentAuditExtensions(deathNotificationSet.hashCode(), "correlationID"));

        when(objectMapper.readValue(sqsMessage.getBody(), GroJsonRecordWithCorrelationID.class)).thenReturn(groJsonRecordWithCorrelationID);
        when(objectMapper.writeValueAsString(deathEnrichmentAudit)).thenReturn("Audit data");

        var deathNotificationSetMapper = mockStatic(DeathNotificationSetMapper.class);
        deathNotificationSetMapper.when(() -> DeathNotificationSetMapper.generateDeathNotificationSet(groJsonRecordWithCorrelationID))
            .thenReturn(deathNotificationSet);

        underTest.handleRequest(sqsEvent, context);

        verify(objectMapper).writeValueAsString(deathEnrichmentAudit);
        verify(awsService).putOnAuditQueue("Audit data");

        deathNotificationSetMapper.close();
    }
}
