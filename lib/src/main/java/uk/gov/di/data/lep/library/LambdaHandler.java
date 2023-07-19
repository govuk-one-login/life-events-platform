package uk.gov.di.data.lep.library;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;


public abstract class LambdaHandler<O> {
    public LambdaLogger logger;
    protected final Config config;
    protected final ObjectMapper objectMapper;

    protected LambdaHandler() {
        this(
            new Config(),
            new Mapper().objectMapper());
    }

    protected LambdaHandler(Config config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Tracing
    public O publish(O output) {
        String message;
        try {
            message = objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            logger.log("Failed to map lambda output to string for publishing");
            throw new RuntimeException(e);
        }

        if (config.getTargetQueue() != null) {
            logger.log("Putting message on target queue: " + config.getTargetQueue());
            AwsService.putOnQueue(message);
        }
        if (config.getTargetTopic() != null) {
            logger.log("Putting message on target topic: " + config.getTargetTopic());
            AwsService.putOnTopic(message);
        }

        return output;
    }
}
