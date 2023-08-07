package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishRecordTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final HttpClient httpClient = mock(HttpClient.class);
    private static final HttpResponse<String> httpResponse = mock(HttpResponse.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final PublishRecord underTest = new PublishRecord(awsService, config, httpClient, objectMapper);
    private static final Context context = mock(Context.class);

    private static final GroJsonRecord event = new GroJsonRecord("1234567890");
    private static final HttpRequest expectedAuthRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://cognitoUri.auth.awsRegion.amazoncognito.com/oauth2/token"))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(
            "grant_type=client_credentials&client_id=cognitoClientId&client_secret=cognitoClientSecret")
        )        .build();
    private static final HttpRequest expectedGroRecordRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://accountUri/events/deathNotification"))
        .header("Authorization", "accessToken")
        .POST(HttpRequest.BodyPublishers.ofString("{\"sourceId\": \"1234567890\"}"))
        .build();

    @BeforeAll
    static void setup() {
        when(config.getAccountUri()).thenReturn("accountUri");
        when(config.getAwsRegion()).thenReturn("awsRegion");
        when(config.getCognitoClientId()).thenReturn("cognitoClientId");
        when(config.getCognitoUri()).thenReturn("cognitoUri");
        when(config.getUserPoolId()).thenReturn("userPoolId");
        when(awsService.getCognitoClientSecret(anyString(), anyString())).thenReturn("cognitoClientSecret");
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
        reset(httpClient);
        reset(objectMapper);
    }

    @Test
    void publishRecordSendsAuthAndGroRecordRequestsAndReturnsNull() throws IOException, InterruptedException {
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("httpBody");
        when(objectMapper.readValue("httpBody", CognitoTokenResponse.class))
            .thenReturn(new CognitoTokenResponse(
                "accessToken",
                "expiresIn",
                "tokenType"
            ));

        var result = underTest.handleRequest(event, context);

        verify(httpClient).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
        assertNull(result);
    }
    @Test
    void publishRecordDoesNotSendGroRecordRequestsIfNoAuthorisationToken() throws IOException, InterruptedException {
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(IOException.class);

        assertThrows(RuntimeException.class, () -> underTest.handleRequest(event, context));
        verify(httpClient, times(1)).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient, never()).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }
    @Test
    void publishRecordThrowExceptionIfGroRecordRequestsFails() throws IOException, InterruptedException {
        when(httpClient.send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("httpBody");
        when(objectMapper.readValue("httpBody", CognitoTokenResponse.class))
            .thenReturn(new CognitoTokenResponse(
                "accessToken",
                "expiresIn",
                "tokenType"
            ));
        when(httpClient.send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString())).thenThrow(IOException.class);

        assertThrows(RuntimeException.class, () -> underTest.handleRequest(event, context));
        verify(httpClient, times(1)).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient, times(1)).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }
}
