package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.data.lep.dto.GroDeathEvent;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;

public class GroDeathValidation
    extends LambdaHandler<GroDeathEventBaseData>
    implements RequestHandler<APIGatewayProxyRequestEvent, GroDeathEventBaseData> {
    @Override
    public GroDeathEventBaseData handleRequest(APIGatewayProxyRequestEvent apiRequest, Context context) {
        var event = validateRequest(apiRequest);
        return publish(event);
    }

    private GroDeathEventBaseData validateRequest(APIGatewayProxyRequestEvent event) {
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
