package uk.gov.di.data.lep.library.services;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.config.Config;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class AwsService {
    private final Config config;
    private static final CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
        .region(Region.EU_WEST_2)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();
    private static final SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
        .region(Region.EU_WEST_2)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();
    private static final SqsClient sqsClient = SqsClient.builder()
        .region(Region.EU_WEST_2)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();
    private static final SnsClient snsClient = SnsClient.builder()
        .region(Region.EU_WEST_2)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();
    private static final S3Client s3Client = S3Client.builder()
        .region(Region.EU_WEST_2)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();

    public AwsService() {
        this(new Config());
    }

    public AwsService(Config config) {
        this.config = config;
    }

    @Tracing
    public void putOnQueue(String message) {
        sqsClient.sendMessage(
            SendMessageRequest.builder()
                .queueUrl(config.getTargetQueue())
                .messageBody(message)
                .build()
        );
    }

    @Tracing
    public void putOnTopic(String message) {
        snsClient.publish(
            PublishRequest.builder()
                .topicArn(config.getTargetTopic())
                .message(message)
                .build()
        );
    }

    @Tracing
    public String getFromBucket(String bucket, String key) {
        var response = s3Client.getObjectAsBytes(
            GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
        );
        return new String(response.asByteArray(), StandardCharsets.UTF_8);
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

    @Tracing
    public void putInBucket(String bucket, String key, File file) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build(),
            RequestBody.fromFile(file)
        );
    }

    @Tracing
    public String getSecret(String secretId) {
        return secretsManagerClient.getSecretValue(GetSecretValueRequest.builder()
                .secretId(secretId)
                .build())
            .secretString();
    }

    @Tracing
    public String getCognitoClientSecret(String userPoolId, String clientId) {
        var client = cognitoClient.describeUserPoolClient(DescribeUserPoolClientRequest.builder()
            .userPoolId(userPoolId)
            .clientId(clientId)
            .build());

        return client.userPoolClient().clientSecret();
    }
}
