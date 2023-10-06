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
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithHeaders;
import uk.gov.di.data.lep.library.dto.RecordLocation;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.GroPublishRecordAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.GroPublishRecordAuditExtensions;
import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Hasher;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GroPublishRecord implements RequestStreamHandler {
    private final AwsService awsService;
    private final Config config;
    private final ObjectMapper objectMapper;
    protected static Logger logger = LogManager.getLogger();

    public GroPublishRecord() {
        this(new AwsService(), new Config(), Mapper.objectMapper());
    }

    public GroPublishRecord(AwsService awsService, Config config, ObjectMapper objectMapper) {
        this.awsService = awsService;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public void handleRequest(InputStream input, OutputStream output, Context context) throws JsonProcessingException {
        var event = getRecord(input);
        logger.info("Received record: registrationID {}, correlationID {}", event.groJsonRecord().registrationID(), event.correlationID());
        audit(event);
        postRecordToLifeEvents(event.groJsonRecord(), event.authenticationToken(), event.correlationID());
    }

    @Tracing
    private GroJsonRecordWithHeaders getRecord(InputStream input) {
        try {
            var recordLocation = objectMapper.readValue(input, RecordLocation.class);
            logger.info("Fetching record: {}", recordLocation.jsonKey());
            return objectMapper.readValue(
                awsService.getFromBucket(recordLocation.jsonBucket(), recordLocation.jsonKey()),
                GroJsonRecordWithHeaders.class
            );
        } catch (IOException e) {
            logger.error("Failed to map Input Stream to GRO JSON record");
            throw new MappingException(e);
        }
    }

    // In this case, the fact that the thread has been interrupted is captured in our message and exception stack,
    // and we do not need to rethrow the same exception
    @SuppressWarnings("java:S2142")
    @Tracing
    private void postRecordToLifeEvents(GroJsonRecord event, String authorisationToken, String correlationID) {
        var httpClient = HttpClient.newHttpClient();
        var requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(String.format("https://%s/events/deathNotification", config.getLifeEventsPlatformDomain())))
            .header("Authorization", authorisationToken)
            .header("CorrelationID", correlationID);

        try {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            logger.error("Failed to map GRO JSON record to string");
            throw new MappingException(e);
        }

        try {
            logger.info("Sending GRO record request: {}", event.registrationID());
            var response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 400) {
                throw new MappingException("Mapping exception during validation");
            }
            if (response.statusCode() != 201) {
                throw new GroApiCallException("Unexpected status code from API Gateway response");
            }
        } catch (IOException | InterruptedException | MappingException e) {
            logger.error("Failed to send GRO record request");
            throw new GroApiCallException("Failed to send GRO record request", e);
        }
    }

    @Tracing
    private void audit(GroJsonRecordWithHeaders event) throws JsonProcessingException {
        var recordHash = Hasher.hash(objectMapper.writeValueAsString(event.groJsonRecord()));
        var auditDataExtensions = new GroPublishRecordAuditExtensions(recordHash, event.correlationID());
        var auditData = new GroPublishRecordAudit(auditDataExtensions);

        try {
            awsService.putOnAuditQueue(objectMapper.writeValueAsString(auditData));
        } catch (JsonProcessingException e) {
            logger.error("Failed to create {} audit log", auditData.eventName());
            throw new MappingException(e);
        }
    }
}
