package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PublishRecord implements RequestHandler<GroJsonRecord, Object> {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    protected static Logger logger = LogManager.getLogger();
    private final AwsService awsService;
    private final Config config;
    private final ObjectMapper mapper;

    public PublishRecord() {
        this(new AwsService(), new Config(), new Mapper().objectMapper());
    }

    public PublishRecord(AwsService awsService, Config config, ObjectMapper mapper) {
        this.awsService = awsService;
        this.config = config;
        this.mapper = mapper;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public Object handleRequest(GroJsonRecord event, Context context) {
        logger.info("Received record: {}", event.RegistrationID());

        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + config.getAccountUri() + "/events/deathNotification"))
                .header("Authorization", getAuthorisationToken())
                .POST(HttpRequest.BodyPublishers.ofString("{\"sourceId\": \"" + event.RegistrationID() + "\"}"))
                .build();

            logger.info("Sending GRO record request: {}", request);
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to send request");
            throw new RuntimeException(e);
        }

        return null;
    }

    private String getAuthorisationToken() throws IOException, InterruptedException {
        logger.info("Sending request to get authorisation token");
        var authorisationRequest = HttpRequest.newBuilder()
            .uri(URI.create(
                "https://"
                    + config.getCognitoUri()
                    + ".auth."
                    + config.getAwsRegion()
                    + ".amazoncognito.com/oauth2/token"
            ))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(
                "grant_type=client_credentials&client_id="
                    + config.getCognitoClientId()
                    + "&client_secret="
                    + awsService.getCognitoClientSecret(config.getUserPoolId(), config.getCognitoClientId()))
            )
            .build();
        var response = httpClient.send(authorisationRequest, HttpResponse.BodyHandlers.ofString());

        return ((mapper.readValue(response.body(), CognitoTokenResponse.class)).access_token());
    }
}
