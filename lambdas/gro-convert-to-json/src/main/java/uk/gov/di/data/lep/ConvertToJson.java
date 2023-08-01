package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.GroFileLocations;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.ArrayList;
import java.util.UUID;

public class ConvertToJson implements RequestHandler<S3ObjectCreatedNotificationEvent, GroFileLocations> {
    protected static Logger logger = LogManager.getLogger();
    private final AwsService awsService;
    private final Config config;
    private final ObjectMapper mapper;

    public ConvertToJson() {
        this(new AwsService(), new Config(), new Mapper().objectMapper());
    }

    public ConvertToJson(AwsService awsService, Config config, ObjectMapper mapper) {
        this.awsService = awsService;
        this.config = config;
        this.mapper = mapper;
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroFileLocations handleRequest(S3ObjectCreatedNotificationEvent event, Context context){
        logger.info("Splitting file");
        logger.info("Bucket: " + event.detail.bucket.name + " has new file: " + event.detail.object.key);
        var jsonRecords = new ArrayList<GroJsonRecord>();
        for (int i = 0; i < 5; i++) {
            var record = new GroJsonRecord();
            record.id = String.valueOf(i);
            jsonRecords.add(record);
        }

        var jsonKey = UUID.randomUUID() + ".json";
        var jsonBucket = config.getGroRecordsBucketName();
        try {
            awsService.putInBucket(jsonBucket, jsonKey, mapper.writeValueAsString(jsonRecords));
        } catch (JsonProcessingException e) {
            logger.error("Failed to map json output to string for publishing");
            throw new RuntimeException(e);
        }

        return new GroFileLocations(event.detail.bucket.name, event.detail.object.key, jsonBucket, jsonKey);
    }
}
