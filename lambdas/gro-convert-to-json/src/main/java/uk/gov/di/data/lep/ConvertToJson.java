package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.DeathRegistrationGroup;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroFileLocations;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.UUID;

public class ConvertToJson implements RequestHandler<S3ObjectCreatedNotificationEvent, GroFileLocations> {
    protected static Logger logger = LogManager.getLogger();
    private final AwsService awsService;
    private final Config config;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public ConvertToJson() {
        this(new AwsService(), new Config(), Mapper.objectMapper(),  Mapper.xmlMapper());
    }

    public ConvertToJson(AwsService awsService, Config config, ObjectMapper objectMapper, XmlMapper xmlMapper) {
        this.awsService = awsService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.xmlMapper = xmlMapper;
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
        try {
            var deathRegistrationGroup = xmlMapper.readValue(xmlData, DeathRegistrationGroup.class);
            return objectMapper.writeValueAsString(deathRegistrationGroup.deathRegistrations);
        } catch (JsonProcessingException e) {
            logger.info("Failed to map DeathRegistrations xml to GroJsonRecord list");
            throw new MappingException(e);
        }
    }
}
