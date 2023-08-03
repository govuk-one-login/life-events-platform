package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.GroDeathEvent;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;
import uk.gov.di.data.lep.library.services.AwsService;

public class GroDeathValidation
    extends LambdaHandler<GroDeathEventBaseData>
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public GroDeathValidation() {
    }

    public GroDeathValidation(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiRequest, Context context) {
        var event = validateRequest(apiRequest);
        publish(event);
        return new APIGatewayProxyResponseEvent().withStatusCode(201);
    }

    @Tracing
    private GroDeathEventBaseData validateRequest(APIGatewayProxyRequestEvent event) {
        logger.info("Validating request");

        GroDeathEvent groDeathEvent;
        try {
            groDeathEvent = objectMapper.readValue(event.getBody(), GroDeathEvent.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to validate request");
            throw new RuntimeException(e);
        }
        var sourceId = groDeathEvent.sourceId();

        if (sourceId == null) {
            logger.warn("sourceId cannot be null");
            throw new IllegalArgumentException("sourceId cannot be null");
        }

        return new GroDeathEventBaseData(sourceId);
    }
}
