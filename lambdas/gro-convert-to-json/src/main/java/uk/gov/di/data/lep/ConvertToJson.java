package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.XML;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.GroFileLocations;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

import java.util.Arrays;
import java.util.UUID;

public class ConvertToJson implements RequestHandler<S3ObjectCreatedNotificationEvent, GroFileLocations> {
    protected static Logger logger = LogManager.getLogger();
    private final AwsService awsService;
    private final Config config;

    public ConvertToJson() {
        this(new AwsService(), new Config());
    }

    public ConvertToJson(AwsService awsService, Config config) {
        this.awsService = awsService;
        this.config = config;
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
        try {
            return deathRegistrationGroup.getJSONArray("DeathRegistration").toString();
        } catch (JSONException e) {
            logger.info(e);
            return (Arrays.toString(new Object[]{deathRegistrationGroup.get("DeathRegistration")}));
        }
    }
}
