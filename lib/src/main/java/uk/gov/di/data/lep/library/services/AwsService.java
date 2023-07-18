package uk.gov.di.data.lep.library.services;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.config.Config;

public class AwsService {
    @Tracing
    public static void putOnQueue(String message) {
        var sqsClient = SqsClient.builder()
            .region(Region.EU_WEST_2)
            .build();

        sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(Config.getTargetQueue())
            .messageBody(message)
            .build());
    }

    @Tracing
    public static void putOnTopic(String message) {
        var snsClient = SnsClient.builder()
            .region(Region.EU_WEST_2)
            .build();

        snsClient.publish(PublishRequest.builder()
            .topicArn(Config.getTargetTopic())
            .message(message)
            .build());
    }
}
