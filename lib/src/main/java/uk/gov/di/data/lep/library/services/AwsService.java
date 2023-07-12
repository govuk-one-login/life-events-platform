package uk.gov.di.data.lep.library.services;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import uk.gov.di.data.lep.library.config.Config;

public class AwsService {

    public static void putOnQueue(String message) {
        var sqsClient = AmazonSQSClientBuilder.standard()
            .withRegion(Regions.EU_WEST_2)
            .build();

        sqsClient.sendMessage(new SendMessageRequest()
            .withQueueUrl(Config.getTargetQueue())
            .withMessageBody(message));
    }

    public static void putOnTopic(String message) {
        var snsClient = AmazonSNSClientBuilder.standard()
            .withRegion(Regions.EU_WEST_2)
            .build();

        snsClient.publish(new PublishRequest()
            .withTopicArn(Config.getTargetTopic())
            .withMessage(message));
    }
}
