package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.data.lep.dto.GroDeathEvent;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;

public class GroDeathValidation
    extends LambdaHandler<GroDeathEventBaseData>
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiRequest, Context context) {
        logger = context.getLogger();
        var event = validateRequest(apiRequest);
        publish(event);
        return new APIGatewayProxyResponseEvent().withStatusCode(201);
    }

    private GroDeathEventBaseData validateRequest(APIGatewayProxyRequestEvent event) {
        logger.log("Validating request");

        GroDeathEvent groDeathEvent;
        try {
            groDeathEvent = new ObjectMapper().readValue(event.getBody(), GroDeathEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var sourceId = groDeathEvent.sourceId();

        if (sourceId == null) {
            throw new IllegalArgumentException("sourceId cannot be null");
        }

        return new GroDeathEventBaseData(sourceId);
    }
}
