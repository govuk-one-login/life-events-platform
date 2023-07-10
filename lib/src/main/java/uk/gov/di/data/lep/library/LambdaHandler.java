package uk.gov.di.data.lep.library;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

public abstract class LambdaHandler<I, O> implements RequestHandler<I, O> {
    @Override
    public O handleRequest(I input, Context context) {

        var output = process(input);
        var awsService = new AwsService<O>();

        if (Config.targetQueue != null) {
            awsService.putOnQueue(output);
        }
        if (Config.targetTopic != null) {
            awsService.putOnTopic(output);
        }
        return output;
    }

    public abstract O process(I input);
}
