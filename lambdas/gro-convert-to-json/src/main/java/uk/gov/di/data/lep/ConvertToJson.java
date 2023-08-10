package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.XML;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.List;
import java.util.UUID;

public class ConvertToJson implements RequestHandler<S3ObjectCreatedNotificationEvent, GroFileLocations> {
    protected static Logger logger = LogManager.getLogger();
    private final AwsService awsService;
    private final Config config;
    private final ObjectMapper objectMapper;

    public ConvertToJson() {
        this(new AwsService(), new Config(), new Mapper().objectMapper());
    }

    public ConvertToJson(AwsService awsService, Config config, ObjectMapper objectMapper) {
        this.awsService = awsService;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroFileLocations handleRequest(S3ObjectCreatedNotificationEvent event, Context context) {
        logger.info("Converting XML to JSON");
        var xmlBucket = event.detail().bucket().name();
        var xmlKey = event.detail().object().key();
        var xmlData = awsService.getFromBucket(xmlBucket, xmlKey);
        var deathRegistrations = convertXmlDataToJson(xmlData);

        var jsonBucket = config.getGroRecordsBucketName();
        var jsonKey = UUID.randomUUID() + ".json";
        logger.info("Putting DeathRegistrations in bucket: {}", jsonBucket);
        awsService.putInBucket(jsonBucket, jsonKey, deathRegistrations);

        return new GroFileLocations(xmlBucket, xmlKey, jsonBucket, jsonKey);
    }

    private String convertXmlDataToJson(String xmlData) {
        var deathRegistrationGroup = XML.toJSONObject(xmlData).getJSONObject("DeathRegistrationGroup");
        var deathRegistrationKey = "DeathRegistration";
        var deathRegistrations = deathRegistrationGroup.optJSONArray(deathRegistrationKey) == null
            ? new JSONArray(List.of(deathRegistrationGroup.get(deathRegistrationKey)))
            : deathRegistrationGroup.getJSONArray(deathRegistrationKey);

        try {
            var registrationRecords = objectMapper.readValue(deathRegistrations.toString(), GroJsonRecord[].class);
            return objectMapper.writeValueAsString(registrationRecords);
        } catch (JsonProcessingException e) {
            logger.info("Failed to map deathRegistrations to GroJsonRecord list");
            throw new MappingException(e);
        }
    }
}
