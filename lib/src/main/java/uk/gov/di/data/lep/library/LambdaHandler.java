package uk.gov.di.data.lep.library;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

public abstract class LambdaHandler<O> {
    protected static Logger logger = LogManager.getLogger();
    protected final AwsService awsService;
    protected final Config config;
    protected final ObjectMapper objectMapper;

    protected LambdaHandler() {
        this(
            new AwsService(),
            new Config(),
            new Mapper().objectMapper());
    }

    protected LambdaHandler(AwsService awsService, Config config, ObjectMapper objectMapper) {
        this.awsService = awsService;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Tracing
    public O publish(O output) {
        String message;
        try {
            message = objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            logger.error("Failed to map lambda output to string for publishing");
            throw new MappingException(e);
        }

        if (config.getTargetQueue() != null) {
            logger.info("Putting message on target queue: {}", config.getTargetQueue());
            awsService.putOnQueue(message);
        }
        if (config.getTargetTopic() != null) {
            logger.info("Putting message on target topic: {}", config.getTargetTopic());
            awsService.putOnTopic(message);
        }

        return output;
    }
}
