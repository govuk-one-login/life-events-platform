package uk.gov.di.data.lep.library;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

public abstract class LambdaHandler<O> {
    public O publish(O output) {
        String message;
        try {
            message = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(output);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (Config.getTargetQueue() != null) {
            AwsService.putOnQueue(message);
        }
        if (Config.getTargetTopic() != null) {
            AwsService.putOnTopic(message);
        }

        return output;
    }
}
