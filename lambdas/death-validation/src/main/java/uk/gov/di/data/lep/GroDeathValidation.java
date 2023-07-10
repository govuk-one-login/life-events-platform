package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import uk.gov.di.data.lep.dto.GroDeathEvent;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;
import com.google.gson.Gson;

public class GroDeathValidation implements RequestHandler<APIGatewayProxyRequestEvent, GroDeathEventBaseData> {

    @Override
    public GroDeathEventBaseData handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        var body = event.getBody();
        var groDeathEvent = new Gson().fromJson(body, GroDeathEvent.class);
        var sourceId = groDeathEvent.sourceId();

        if (sourceId == null) {
            throw new IllegalArgumentException("sourceId cannot be null");
        }

        return new GroDeathEventBaseData(sourceId);

    }
}
