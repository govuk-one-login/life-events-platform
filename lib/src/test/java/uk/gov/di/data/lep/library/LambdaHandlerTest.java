package uk.gov.di.data.lep.library;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedDataBuilder;
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
    private static final LambdaHandler<GroDeathEventEnrichedData> underTest = new TestLambda(awsService, config, objectMapper);

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
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        var mapper = mockStatic(Mapper.class);
        new TestLambda();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        mapper.verify(Mapper::objectMapper, times(1));
        mapper.close();
    }

    @Test
    void failingToWriteAsStringThrowsException() throws JsonProcessingException {
        var output = new GroDeathEventEnrichedDataBuilder().build();

        var jsonException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(output)).thenThrow(jsonException);

        var exception = assertThrows(MappingException.class, () -> underTest.publish(output));

        assertEquals(jsonException, exception.getCause());

        verify(awsService, never()).putOnQueue(any());
        verify(awsService, never()).putOnTopic(any());
    }

    @Test
    void publishPublishesMessageToQueue() throws JsonProcessingException {
        var output = new GroDeathEventEnrichedDataBuilder().build();

        when(config.getTargetQueue()).thenReturn("targetQueueURL");
        when(objectMapper.writeValueAsString(output)).thenReturn("mappedQueueOutput");

        underTest.publish(output);

        verify(logger).info("Putting message on target queue: {}", "targetQueueURL");

        verify(awsService).putOnQueue("mappedQueueOutput");
    }

    @Test
    void publishPublishesMessageToTopic() throws JsonProcessingException {
        var output = new GroDeathEventEnrichedDataBuilder().build();

        when(config.getTargetTopic()).thenReturn("targetTopicARN");
        when(objectMapper.writeValueAsString(output)).thenReturn("mappedTopicOutput");

        underTest.publish(output);

        verify(logger).info("Putting message on target topic: {}", "targetTopicARN");

        verify(awsService).putOnTopic("mappedTopicOutput");
    }

    static class TestLambda extends LambdaHandler<GroDeathEventEnrichedData> {
        public TestLambda() {
            super();
        }

        public TestLambda(AwsService awsService, Config config, ObjectMapper objectMapper) {
            super(awsService, config, objectMapper);
        }
    }
}
