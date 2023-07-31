package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;

public class PublishRecord implements RequestHandler<GroJsonRecord, Object> {
    protected static Logger logger = LogManager.getLogger();

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroJsonRecord handleRequest(GroJsonRecord event, Context context) {
        logger.info("Received xml record");
        return null;
    }
}
