package uk.gov.di.data.lep.library;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.DeathValidationAuditExtensions;
import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LambdaHandlerTest {
    private static final Config config = mock(Config.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final Logger logger = mock(Logger.class);
    private static final AwsService awsService = mock(AwsService.class);
    private static final LambdaHandler<GroJsonRecord> underTest = new TestLambda(awsService, config, objectMapper);

    @BeforeAll
    public static void setup() {
        underTest.logger = logger;
    }

    @BeforeEach
    public void refreshSetup() {
        reset(config);
        reset(logger);
        reset(objectMapper);
        clearInvocations(awsService);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new TestLambda();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.close();
        }
    }

    @Test
    void publishFailingToWriteAsStringThrowsException() throws JsonProcessingException {
        var output = new GroJsonRecordBuilder().build();

        var jsonException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(output)).thenThrow(jsonException);

        var exception = assertThrows(MappingException.class, () -> underTest.mapAndPublish(output));

        assertEquals(jsonException, exception.getCause());

        verify(awsService, never()).putOnQueue(any());
        verify(awsService, never()).putOnTopic(any());
    }

    @Test
    void publishPublishesMessageToQueue() throws JsonProcessingException {
        var output = new GroJsonRecordBuilder().build();

        when(config.getTargetQueue()).thenReturn("targetQueueURL");
        when(objectMapper.writeValueAsString(output)).thenReturn("mappedQueueOutput");

        underTest.mapAndPublish(output);

        verify(logger).info("Putting message on target queue: {}", "targetQueueURL");

        verify(awsService).putOnQueue("mappedQueueOutput");
    }

    @Test
    void publishPublishesMessageToTopic() throws JsonProcessingException {
        var output = new GroJsonRecordBuilder().build();

        when(config.getTargetTopic()).thenReturn("targetTopicARN");
        when(objectMapper.writeValueAsString(output)).thenReturn("mappedTopicOutput");

        underTest.mapAndPublish(output);

        verify(logger).info("Putting message on target topic: {}", "targetTopicARN");

        verify(awsService).putOnTopic("mappedTopicOutput");
    }

    @Test
    void addAuditDataToQueuePublishesMessageToQueue() throws JsonProcessingException {
        var auditData = new DeathValidationAudit(new DeathValidationAuditExtensions("correlationID"));

        when(config.getAuditQueue()).thenReturn("auditQueue");
        when(objectMapper.writeValueAsString(auditData)).thenReturn("mappedAuditData");

        underTest.addAuditDataToQueue(auditData);

        verify(awsService).putOnAuditQueue("mappedAuditData");
    }

    @Test
    void addAuditDataToQueueFailingToWriteAsStringThrowsException() throws JsonProcessingException {
        var auditData = new DeathValidationAudit(new DeathValidationAuditExtensions("correlationID"));

        var jsonException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(auditData)).thenThrow(jsonException);

        var exception = assertThrows(MappingException.class, () -> underTest.addAuditDataToQueue(auditData));

        assertEquals(jsonException, exception.getCause());

        verify(awsService, never()).putOnAuditQueue(any());
    }

    static class TestLambda extends LambdaHandler<GroJsonRecord> {
        public TestLambda() {
            super();
        }

        public TestLambda(AwsService awsService, Config config, ObjectMapper objectMapper) {
            super(awsService, config, objectMapper);
        }
    }
}
