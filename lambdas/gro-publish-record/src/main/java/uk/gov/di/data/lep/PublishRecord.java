package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class PublishRecord implements RequestHandler<GroJsonRecord, Object> {
    protected static Logger logger = LogManager.getLogger();

    @Override
    @Tracing
    @Logging(clearState = true)
    public APIGatewayProxyRequestEvent handleRequest(GroJsonRecord event, Context context) {
        logger.info("Received record");
        var httpClient = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
            .uri("https://hanleo.life-events.dev.account.gov.uk/events/deathNotification")
            .header(Map.of("Authorization", "Bearer jwt"))
            .POST(HttpRequest.BodyPublishers.ofString("{\"sourceId\": " + event.registrationId + "}"))
            .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info(request);

        return request;
    }
}
