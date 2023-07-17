package uk.gov.di.data.lep.library;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

public abstract class LambdaHandler<O> {
    public LambdaLogger logger;
    public O publish(O output) {
        String message;
        try {
            message = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(output);
        } catch (JsonProcessingException e) {
            logger.log("Failed to map lambda output to string for publishing");
            throw new RuntimeException(e);
        }

        if (Config.getTargetQueue() != null) {
            logger.log("Putting message on target queue: " + Config.getTargetQueue());
            AwsService.putOnQueue(message);
        }
        if (Config.getTargetTopic() != null) {
            logger.log("Putting message on target topic: " + Config.getTargetTopic());
            AwsService.putOnTopic(message);
        }

        return output;
    }
}
