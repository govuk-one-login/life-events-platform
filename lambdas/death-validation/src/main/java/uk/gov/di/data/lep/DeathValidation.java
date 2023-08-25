package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

public class DeathValidation
    extends LambdaHandler<GroJsonRecord>
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public DeathValidation() {
    }

    public DeathValidation(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiRequest, Context context) {
        try {
            var event = validateRequest(apiRequest);
            publish(event);
            return new APIGatewayProxyResponseEvent().withStatusCode(201);
        } catch (MappingException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }
    }

    @Tracing
    private GroJsonRecord validateRequest(APIGatewayProxyRequestEvent event) {
        logger.info("Validating request");

        try {
            return objectMapper.readValue(event.getBody(), GroJsonRecord.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to validate request due to mapping error");
            throw new MappingException(e);
        }
    }
}
