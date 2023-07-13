package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Authorisation implements RequestHandler {

    @Override
    public Object handleRequest(Object input, Context context) {
        var logger = context.getLogger();
        logger.log("Authentication and authorising request");
        return null;
    }
}
