package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.exceptions.GroApiCallException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithAuth;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GroPublishRecord implements RequestStreamHandler {
    private final Config config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    protected static Logger logger = LogManager.getLogger();

    public GroPublishRecord() {
        this(new Config(), HttpClient.newHttpClient(), Mapper.objectMapper());
    }

    public GroPublishRecord(Config config, HttpClient httpClient, ObjectMapper objectMapper) {
        this.config = config;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public void handleRequest(InputStream input, OutputStream output, Context context) {
        var event = readInputStream(input);
        logger.info("Received record: {}", event.groJsonRecord().registrationID());
        postRecordToLifeEvents(event.groJsonRecord(), event.authenticationToken());
    }

    @Tracing
    private GroJsonRecordWithAuth readInputStream(InputStream input) {
        try {
            return objectMapper.readValue(input, GroJsonRecordWithAuth.class);
        } catch (IOException e) {
            logger.error("Failed to map Input Stream to GRO JSON record");
            throw new MappingException(e);
        }
    }

    // In this case, the fact that the thread has been interrupted is captured in our message and exception stack,
    // and we do not need to rethrow the same exception
    @SuppressWarnings("java:S2142")
    @Tracing
    private void postRecordToLifeEvents(GroJsonRecord event, String authorisationToken) {

        var requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(String.format("https://%s/events/deathNotification", config.getLifeEventsPlatformDomain())))
            .header("Authorization", authorisationToken);

        try {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            logger.error("Failed to map GRO JSON record to string");
            throw new MappingException(e);
        }

        try {
            logger.info("Sending GRO record request: {}", event.registrationID());
            httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to send GRO record request");
            throw new GroApiCallException("Failed to send GRO record request", e);
        }
    }
}
