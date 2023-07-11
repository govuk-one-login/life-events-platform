package uk.gov.di.data.lep.library;

import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

public abstract class LambdaHandler<O> {
    public O publish(O output) {
        var awsService = new AwsService<O>();

        if (Config.targetQueue != null) {
            awsService.putOnQueue(output);
        }
        if (Config.targetTopic != null) {
            awsService.putOnTopic(output);
        }
        return output;
    }
}
