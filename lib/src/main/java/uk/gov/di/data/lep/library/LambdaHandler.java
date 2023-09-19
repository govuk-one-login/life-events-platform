package uk.gov.di.data.lep.library;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.BaseAudit;
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
            Mapper.objectMapper());
    }

    protected LambdaHandler(AwsService awsService, Config config, ObjectMapper objectMapper) {
        this.awsService = awsService;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Tracing
    public String mapAndPublish(O output) {
        String message;
        try {
            message = objectMapper.writeValueAsString(output);
        } catch (JsonProcessingException e) {
            logger.error("Failed to map lambda output to string for publishing");
            throw new MappingException(e);
        }
        return publish(message);
    }

    @Tracing
    public String publish(String message) {
        if (config.getTargetQueue() != null) {
            logger.info("Putting message on target queue: {}", config.getTargetQueue());
            awsService.putOnQueue(message);
        }
        if (config.getTargetTopic() != null) {
            logger.info("Putting message on target topic: {}", config.getTargetTopic());
            awsService.putOnTopic(message);
        }
        return message;
    }

    @Tracing
    public <T extends BaseAudit> void addAuditDataToQueue(T auditData) {
        try {
            awsService.putOnAuditQueue(objectMapper.writeValueAsString(auditData));
        } catch (JsonProcessingException e) {
            logger.info("Failed to create {} audit log", auditData.eventName());
            throw new MappingException(e);
        }
    }
}
