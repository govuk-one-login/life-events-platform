package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;

public class Authorisation implements RequestHandler<Object, Object> {

    @Override
    @Tracing
    @Logging(clearState = true)
    public Object handleRequest(Object input, Context context) {
        var logger = context.getLogger();
        logger.log("Authenticating and authorising request");
        return null;
    }
}
