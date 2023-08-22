package uk.gov.di.data.lep.library.services;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import uk.gov.di.data.lep.library.config.Config;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AwsServiceTest {
    private final Config config = mock(Config.class);

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var config = mockConstruction(Config.class)) {
            new AwsService();
            assertEquals(1, config.constructed().size());
        }
    }

    @Test
    void putOnQueuePutsMessageOnQueue() {
        try (var staticClient = mockStatic(SqsClient.class);
             var staticCredentialsProvider = mockStatic(DefaultCredentialsProvider.class);
             var staticRequest = mockStatic(SendMessageRequest.class)) {

            var client = mock(SqsClient.class);
            var clientBuilder = mock(SqsClientBuilder.class);
            var credentialsProvider = mock(DefaultCredentialsProvider.class);
            var request = mock(SendMessageRequest.class);
            var requestBuilder = mock(SendMessageRequest.Builder.class);

            staticClient.when(SqsClient::builder).thenReturn(clientBuilder);
            staticCredentialsProvider.when(DefaultCredentialsProvider::create).thenReturn(credentialsProvider);
            staticRequest.when(SendMessageRequest::builder).thenReturn(requestBuilder);

            when(config.getTargetQueue()).thenReturn("Target Queue");

            when(clientBuilder.region(Region.EU_WEST_2)).thenReturn(clientBuilder);
            when(clientBuilder.credentialsProvider(credentialsProvider)).thenReturn(clientBuilder);
            when(clientBuilder.build()).thenReturn(client);
            when(requestBuilder.queueUrl("Target Queue")).thenReturn(requestBuilder);
            when(requestBuilder.messageBody("Message body")).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            var underTest = new AwsService(config);

            underTest.putOnQueue("Message body");

            verify(clientBuilder, times(1)).region(Region.EU_WEST_2);
            verify(clientBuilder, times(1)).credentialsProvider(credentialsProvider);
            verify(requestBuilder, times(1)).queueUrl("Target Queue");
            verify(requestBuilder, times(1)).messageBody("Message body");
            verify(client, times(1)).sendMessage(request);
        }
    }

    @Test
    void putOnTopicPutsMessageOnTopic() {
        try (var staticClient = mockStatic(SnsClient.class);
             var staticCredentialsProvider = mockStatic(DefaultCredentialsProvider.class);
             var staticRequest = mockStatic(PublishRequest.class)) {

            var client = mock(SnsClient.class);
            var clientBuilder = mock(SnsClientBuilder.class);
            var credentialsProvider = mock(DefaultCredentialsProvider.class);
            var request = mock(PublishRequest.class);
            var requestBuilder = mock(PublishRequest.Builder.class);

            staticClient.when(SnsClient::builder).thenReturn(clientBuilder);
            staticCredentialsProvider.when(DefaultCredentialsProvider::create).thenReturn(credentialsProvider);
            staticRequest.when(PublishRequest::builder).thenReturn(requestBuilder);

            when(config.getTargetTopic()).thenReturn("Target Topic");

            when(clientBuilder.region(Region.EU_WEST_2)).thenReturn(clientBuilder);
            when(clientBuilder.credentialsProvider(credentialsProvider)).thenReturn(clientBuilder);
            when(clientBuilder.build()).thenReturn(client);
            when(requestBuilder.topicArn("Target Topic")).thenReturn(requestBuilder);
            when(requestBuilder.message("Message body")).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            var underTest = new AwsService(config);

            underTest.putOnTopic("Message body");

            verify(clientBuilder, times(1)).region(Region.EU_WEST_2);
            verify(clientBuilder, times(1)).credentialsProvider(credentialsProvider);
            verify(requestBuilder, times(1)).topicArn("Target Topic");
            verify(requestBuilder, times(1)).message("Message body");
            verify(client, times(1)).publish(request);
        }
    }

    @Test
    void getFromBucketGetsStringFromBucket() {
        try (var staticClient = mockStatic(S3Client.class);
             var staticCredentialsProvider = mockStatic(DefaultCredentialsProvider.class);
             var staticRequest = mockStatic(GetObjectRequest.class)) {
            var client = mock(S3Client.class);
            var clientBuilder = mock(S3ClientBuilder.class);
            var credentialsProvider = mock(DefaultCredentialsProvider.class);
            var request = mock(GetObjectRequest.class);
            var requestBuilder = mock(GetObjectRequest.Builder.class);

            var responseBytes = mock(ResponseBytes.class);

            staticClient.when(S3Client::builder).thenReturn(clientBuilder);
            staticCredentialsProvider.when(DefaultCredentialsProvider::create).thenReturn(credentialsProvider);
            staticRequest.when(GetObjectRequest::builder).thenReturn(requestBuilder);

            when(clientBuilder.region(Region.EU_WEST_2)).thenReturn(clientBuilder);
            when(clientBuilder.credentialsProvider(credentialsProvider)).thenReturn(clientBuilder);
            when(clientBuilder.build()).thenReturn(client);
            when(requestBuilder.bucket("Bucket")).thenReturn(requestBuilder);
            when(requestBuilder.key("Key")).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);
            when(client.getObjectAsBytes(request)).thenReturn(responseBytes);
            when(responseBytes.asByteArray()).thenReturn("ByteArray".getBytes());

            var underTest = new AwsService(config);

            var object = underTest.getFromBucket("Bucket", "Key");

            verify(clientBuilder, times(1)).region(Region.EU_WEST_2);
            verify(clientBuilder, times(1)).credentialsProvider(credentialsProvider);
            verify(requestBuilder, times(1)).bucket("Bucket");
            verify(requestBuilder, times(1)).key("Key");
            verify(client, times(1)).getObjectAsBytes(request);

            assertEquals("ByteArray", object);
        }
    }

    @Test
    void putInBucketPutsStringInBucket() {
        try (var staticClient = mockStatic(S3Client.class);
             var staticCredentialsProvider = mockStatic(DefaultCredentialsProvider.class);
             var staticRequest = mockStatic(PutObjectRequest.class);
             var staticRequestBody = mockStatic(RequestBody.class)) {
            var client = mock(S3Client.class);
            var clientBuilder = mock(S3ClientBuilder.class);
            var credentialsProvider = mock(DefaultCredentialsProvider.class);
            var request = mock(PutObjectRequest.class);
            var requestBuilder = mock(PutObjectRequest.Builder.class);
            var requestBody = mock(RequestBody.class);

            staticClient.when(S3Client::builder).thenReturn(clientBuilder);
            staticCredentialsProvider.when(DefaultCredentialsProvider::create).thenReturn(credentialsProvider);
            staticRequest.when(PutObjectRequest::builder).thenReturn(requestBuilder);

            staticRequestBody.when(() -> RequestBody.fromString("Content")).thenReturn(requestBody);

            when(clientBuilder.region(Region.EU_WEST_2)).thenReturn(clientBuilder);
            when(clientBuilder.credentialsProvider(credentialsProvider)).thenReturn(clientBuilder);
            when(clientBuilder.build()).thenReturn(client);
            when(requestBuilder.bucket("Bucket")).thenReturn(requestBuilder);
            when(requestBuilder.key("Key")).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            var underTest = new AwsService(config);

            underTest.putInBucket("Bucket", "Key", "Content");

            verify(clientBuilder, times(1)).region(Region.EU_WEST_2);
            verify(clientBuilder, times(1)).credentialsProvider(credentialsProvider);
            verify(requestBuilder, times(1)).bucket("Bucket");
            verify(requestBuilder, times(1)).key("Key");
            verify(client, times(1)).putObject(request, requestBody);
        }
    }

    @Test
    void putInBucketPutsStreamInBucket() {
        try (var staticClient = mockStatic(S3Client.class);
             var staticCredentialsProvider = mockStatic(DefaultCredentialsProvider.class);
             var staticRequest = mockStatic(PutObjectRequest.class);
             var staticRequestBody = mockStatic(RequestBody.class)) {
            var client = mock(S3Client.class);
            var clientBuilder = mock(S3ClientBuilder.class);
            var credentialsProvider = mock(DefaultCredentialsProvider.class);
            var request = mock(PutObjectRequest.class);
            var requestBuilder = mock(PutObjectRequest.Builder.class);
            var requestBody = mock(RequestBody.class);

            var inputStream = mock(InputStream.class);

            staticClient.when(S3Client::builder).thenReturn(clientBuilder);
            staticCredentialsProvider.when(DefaultCredentialsProvider::create).thenReturn(credentialsProvider);
            staticRequest.when(PutObjectRequest::builder).thenReturn(requestBuilder);

            staticRequestBody.when(() -> RequestBody.fromInputStream(inputStream, 10)).thenReturn(requestBody);

            when(clientBuilder.region(Region.EU_WEST_2)).thenReturn(clientBuilder);
            when(clientBuilder.credentialsProvider(credentialsProvider)).thenReturn(clientBuilder);
            when(clientBuilder.build()).thenReturn(client);
            when(requestBuilder.bucket("Bucket")).thenReturn(requestBuilder);
            when(requestBuilder.key("Key")).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            var underTest = new AwsService(config);

            underTest.putInBucket("Bucket", "Key", inputStream, 10);

            verify(clientBuilder, times(1)).region(Region.EU_WEST_2);
            verify(clientBuilder, times(1)).credentialsProvider(credentialsProvider);
            verify(requestBuilder, times(1)).bucket("Bucket");
            verify(requestBuilder, times(1)).key("Key");
            verify(client, times(1)).putObject(request, requestBody);
        }
    }

    @Test
    void getSecretGetsSecretValue() {
        try (var staticClient = mockStatic(SecretsManagerClient.class);
             var staticCredentialsProvider = mockStatic(DefaultCredentialsProvider.class);
             var staticRequest = mockStatic(GetSecretValueRequest.class)) {
            var client = mock(SecretsManagerClient.class);
            var clientBuilder = mock(SecretsManagerClientBuilder.class);
            var credentialsProvider = mock(DefaultCredentialsProvider.class);
            var request = mock(GetSecretValueRequest.class);
            var requestBuilder = mock(GetSecretValueRequest.Builder.class);

            var response = mock(GetSecretValueResponse.class);

            staticClient.when(SecretsManagerClient::builder).thenReturn(clientBuilder);
            staticCredentialsProvider.when(DefaultCredentialsProvider::create).thenReturn(credentialsProvider);
            staticRequest.when(GetSecretValueRequest::builder).thenReturn(requestBuilder);

            when(clientBuilder.region(Region.EU_WEST_2)).thenReturn(clientBuilder);
            when(clientBuilder.credentialsProvider(credentialsProvider)).thenReturn(clientBuilder);
            when(clientBuilder.build()).thenReturn(client);
            when(requestBuilder.secretId("SecretId")).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);
            when(client.getSecretValue(request)).thenReturn(response);
            when(response.secretString()).thenReturn("Secret");

            var underTest = new AwsService(config);

            var secret = underTest.getSecret("SecretId");

            verify(clientBuilder, times(1)).region(Region.EU_WEST_2);
            verify(clientBuilder, times(1)).credentialsProvider(credentialsProvider);
            verify(requestBuilder, times(1)).secretId("SecretId");
            verify(client, times(1)).getSecretValue(request);

            assertEquals("Secret", secret);
        }
    }

    @Test
    void getCognitoClientSecretGetsClientSecret() {
        try (var staticClient = mockStatic(CognitoIdentityProviderClient.class);
             var staticCredentialsProvider = mockStatic(DefaultCredentialsProvider.class);
             var staticRequest = mockStatic(DescribeUserPoolClientRequest.class)) {
            var client = mock(CognitoIdentityProviderClient.class);
            var clientBuilder = mock(CognitoIdentityProviderClientBuilder.class);
            var credentialsProvider = mock(DefaultCredentialsProvider.class);
            var request = mock(DescribeUserPoolClientRequest.class);
            var requestBuilder = mock(DescribeUserPoolClientRequest.Builder.class);

            var response = mock(DescribeUserPoolClientResponse.class);
            var userPoolClient = mock(UserPoolClientType.class);

            staticClient.when(CognitoIdentityProviderClient::builder).thenReturn(clientBuilder);
            staticCredentialsProvider.when(DefaultCredentialsProvider::create).thenReturn(credentialsProvider);
            staticRequest.when(DescribeUserPoolClientRequest::builder).thenReturn(requestBuilder);

            when(clientBuilder.region(Region.EU_WEST_2)).thenReturn(clientBuilder);
            when(clientBuilder.credentialsProvider(credentialsProvider)).thenReturn(clientBuilder);
            when(clientBuilder.build()).thenReturn(client);
            when(requestBuilder.userPoolId("UserPoolId")).thenReturn(requestBuilder);
            when(requestBuilder.clientId("ClientId")).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);
            when(client.describeUserPoolClient(request)).thenReturn(response);
            when(response.userPoolClient()).thenReturn(userPoolClient);
            when(userPoolClient.clientSecret()).thenReturn("ClientSecret");

            var underTest = new AwsService(config);

            var secret = underTest.getCognitoClientSecret("UserPoolId", "ClientId");

            verify(clientBuilder, times(1)).region(Region.EU_WEST_2);
            verify(clientBuilder, times(1)).credentialsProvider(credentialsProvider);
            verify(requestBuilder, times(1)).userPoolId("UserPoolId");
            verify(requestBuilder, times(1)).clientId("ClientId");
            verify(client, times(1)).describeUserPoolClient(request);

            assertEquals("ClientSecret", secret);
        }
    }
}
