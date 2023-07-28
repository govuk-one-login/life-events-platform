package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;

public class SplitFile implements RequestHandler<S3ObjectCreatedNotificationEvent, Object> {
    protected static Logger logger = LogManager.getLogger();

    @Override
    @Tracing
    @Logging(clearState = true, logEvent = true)
    public Object handleRequest(S3ObjectCreatedNotificationEvent event, Context context) {
        logger.info("Splitting file");
        logger.info("Bucket: " + event.detail.bucket.name + " has new file: " + event.detail.object.key);
        return null;
    }
}
