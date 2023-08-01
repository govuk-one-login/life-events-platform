package uk.gov.di.data.lep.library.services;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.config.Config;

public class AwsService {
    private static Config config;
    private final static SqsClient sqsClient = SqsClient.builder()
        .region(Region.EU_WEST_2)
        .build();
    private final static SnsClient snsClient = SnsClient.builder()
        .region(Region.EU_WEST_2)
        .build();
    private final static S3Client s3Client = S3Client.builder()
        .region(Region.EU_WEST_2)
        .build();

    public AwsService() {
        this(new Config());
    }

    public AwsService(Config _config) {
        config = _config;
    }

    @Tracing
    public void putOnQueue(String message) {
        sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(config.getTargetQueue())
            .messageBody(message)
            .build());
    }

    @Tracing
    public void putOnTopic(String message) {
        snsClient.publish(PublishRequest.builder()
            .topicArn(config.getTargetTopic())
            .message(message)
            .build());
    }

    @Tracing
    public void putInBucket(String bucket, String key, String file) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build(),
            RequestBody.fromString(file)
        );
    }
}
