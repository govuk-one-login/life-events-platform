package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.cloudwatchlogs.emf.model.Unit;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.metrics.MetricsUtils;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.CognitoTokenResponse;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.exceptions.AuthException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroFileLocations;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithAuth;
import uk.gov.di.data.lep.library.dto.gro.DeathRegistrationGroup;
import uk.gov.di.data.lep.library.dto.gro.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class GroConvertToJson implements RequestHandler<S3ObjectCreatedNotificationEvent, GroFileLocations> {
    protected static Logger logger = LogManager.getLogger();
    protected static MetricsLogger metricsLogger = MetricsUtils.metricsLogger();
    private final AwsService awsService;
    private final Config config;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public GroConvertToJson() {
        this(new AwsService(), new Config(), Mapper.objectMapper(), Mapper.xmlMapper());
    }

    public GroConvertToJson(AwsService awsService, Config config, ObjectMapper objectMapper, XmlMapper xmlMapper) {
        this.awsService = awsService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.xmlMapper = xmlMapper;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    @Metrics(captureColdStart = true)
    public GroFileLocations handleRequest(S3ObjectCreatedNotificationEvent event, Context context) {
        var authorisationToken = getAuthorisationToken();
        logger.info("Converting XML to JSON");
        var xmlBucket = event.detail().bucket().name();
        var xmlKey = event.detail().object().key();
        var xmlData = awsService.getFromBucket(xmlBucket, xmlKey);
        var deathRegistrations = convertXmlDataToJson(xmlData, authorisationToken);

        var jsonBucket = config.getGroRecordsBucketName();
        var jsonKey = UUID.randomUUID() + ".json";
        logger.info("Putting DeathRegistrations in bucket: {}", jsonBucket);
        awsService.putInBucket(jsonBucket, jsonKey, deathRegistrations);

        return new GroFileLocations(xmlBucket, xmlKey, jsonBucket, jsonKey);
    }

    // In this case, the fact that the thread has been interrupted is captured in our message and exception stack,
    // and we do not need to rethrow the same exception
    @SuppressWarnings("java:S2142")
    @Tracing
    private String getAuthorisationToken() {
        var httpClient = HttpClient.newHttpClient();
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
            logger.info("Sending authorisation request");
            var response = httpClient.send(authorisationRequest, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), CognitoTokenResponse.class).accessToken();
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to send authorisation request");
            throw new AuthException("Failed to send authorisation request", e);
        }
    }

    private String convertXmlDataToJson(String xmlData, String authorisationToken) {
        try {
            var deathRegistrationGroup = xmlMapper.readValue(xmlData, DeathRegistrationGroup.class);
            var records = deathRegistrationGroup.deathRegistrations();

            validateRecordCount(records, deathRegistrationGroup.recordCount());

            var recordsWithHeaders = records.stream()
                .map(r -> new GroJsonRecordWithHeaders(r, authorisationToken, UUID.randomUUID().toString()))
                .toList();

            recordsWithHeaders.forEach(record -> addAuditDataToQueue(generateAuditData(record.correlationId(), xmlData.hashCode())));

            return objectMapper.writeValueAsString(recordsWithHeaders);
        } catch (JsonProcessingException e) {
            logger.info("Failed to map DeathRegistrations xml to GroJsonRecord list");
            throw new MappingException(e);
        }
    }

    private void validateRecordCount(List<GroJsonRecord> records, int recordCount) {
        metricsLogger.putMetric("SOURCE_RECORDS_EXPECTED", recordCount, Unit.COUNT);

        if (records == null) {
            if (recordCount == 0) {
                throw new MappingException("File contains no registration records");
            } else {
                throw new MappingException(String.format("Expected %d records but none were found", recordCount));
            }
        } else {
            metricsLogger.putMetric("SOURCE_RECORDS_ACTUAL", records.size(), Unit.COUNT);
            if (records.size() != recordCount) {
                throw new MappingException(String.format("Expected %d records but %d were found", recordCount, records.size()));
            }
        }
    }
}
