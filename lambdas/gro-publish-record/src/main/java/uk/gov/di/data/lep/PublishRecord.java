package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.CognitoTokenResponse;
import uk.gov.di.data.lep.exceptions.GroApiCallException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.exceptions.AuthException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PublishRecord implements RequestHandler<GroJsonRecord, Object> {
    private final AwsService awsService;
    private final Config config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    protected static Logger logger = LogManager.getLogger();

    public PublishRecord() {
        this(new AwsService(), new Config(), HttpClient.newHttpClient(), new Mapper().objectMapper());
    }

    public PublishRecord(AwsService awsService, Config config, HttpClient httpClient, ObjectMapper objectMapper) {
        this.awsService = awsService;
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public Object handleRequest(GroJsonRecord event, Context context) {
        logger.info("Received record: {}", event.RegistrationID());
        var authorisationToken = getAuthorisationToken();
        postRecordToLifeEvents(event, authorisationToken);
        return null;
    }

    private String getAuthorisationToken() {
        logger.info("Sending request to get authorisation token");
        var clientId = config.getCognitoClientId();
        var clientSecret = awsService.getCognitoClientSecret(config.getUserPoolId(), clientId);
        var authorisationRequest = HttpRequest.newBuilder()
            .uri(URI.create(config.getCognitoOauth2TokenUri()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(String.format(
                "grant_type=client_credentials&client_id=%s&client_secret=%s",
                clientId,
                clientSecret
            )))
            .build();

        try {
            var response = httpClient.send(authorisationRequest, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), CognitoTokenResponse.class).accessToken();
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to send authorisation request");
            Thread.currentThread().interrupt();
            throw new AuthException("Failed to send authorisation request", e);
        }
    }

    private void postRecordToLifeEvents(GroJsonRecord event, String authorisationToken) {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(String.format("https://%s/events/deathNotification", config.getLifeEventsPlatformDomain())))
            .header("Authorization", authorisationToken)
            .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"sourceId\": \"%s\"}", event.RegistrationID())))
            .build();

        try {
            logger.info("Sending GRO record request: {}", event.RegistrationID());
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw handleGroApiCallException(e);
        }
    }
    private GroApiCallException handleGroApiCallException(Exception e){
        logger.error("Failed to send GRO record request");
        Thread.currentThread().interrupt();
        return new GroApiCallException("Failed to send GRO record request", e);
    }
}
