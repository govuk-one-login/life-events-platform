package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.dto.CognitoTokenResponse;
import uk.gov.di.data.lep.exceptions.AuthException;
import uk.gov.di.data.lep.exceptions.GroApiCallException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishRecordTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final HttpClient httpClient = mock(HttpClient.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final PublishRecord underTest = new PublishRecord(awsService, config, httpClient, objectMapper);
    private static final Context context = mock(Context.class);
    private static final GroJsonRecord event = new GroJsonRecordBuilder().build();
    private static final String eventAsString = "EventInStringRepresentation";
    private static final String cognitoClientId = "cognitoClientId";
    private static final String cognitoClientSecret = "cognitoClientSecret";
    private static final String cognitoOauth2TokenUri = "https://cognitoDomainName.auth.awsRegion.amazoncognito.com/oauth2/token";
    private static final HttpRequest expectedAuthRequest = HttpRequest.newBuilder()
        .uri(URI.create(cognitoOauth2TokenUri))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(
            String.format("grant_type=client_credentials&client_id=%s&client_secret=%s", cognitoClientId, cognitoClientSecret))
        ).build();
    private static final HttpRequest expectedGroRecordRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://lifeEventsPlatformDomain/events/deathNotification"))
        .header("Authorization", "accessToken")
        .POST(HttpRequest.BodyPublishers.ofString(eventAsString))
        .build();
    private static final HttpResponse<String> httpResponse = mock(HttpResponse.class);

    @BeforeAll
    static void setup() {
        when(config.getLifeEventsPlatformDomain()).thenReturn("lifeEventsPlatformDomain");
        when(config.getCognitoClientId()).thenReturn(cognitoClientId);
        when(config.getCognitoOauth2TokenUri()).thenReturn(cognitoOauth2TokenUri);
        when(config.getUserPoolId()).thenReturn("userPoolId");
        when(awsService.getCognitoClientSecret(anyString(), anyString())).thenReturn(cognitoClientSecret);
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
        reset(httpClient);
        reset(objectMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        var httpClient = mockStatic(HttpClient.class);
        var mapper = mockStatic(Mapper.class);
        new PublishRecord();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        httpClient.verify(HttpClient::newHttpClient, times(1));
        mapper.verify(Mapper::objectMapper, times(1));
        httpClient.close();
        mapper.close();
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
        when(objectMapper.writeValueAsString(event)).thenReturn(eventAsString);

        var result = underTest.handleRequest(event, context);

        verify(httpClient).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
        assertNull(result);
    }

    @Test
    void publishRecordDoesNotSendGroRecordRequestsIfNoAuthorisationToken() throws IOException, InterruptedException {
        var ioException = new IOException();
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(ioException);

        var exception = assertThrows(AuthException.class, () -> underTest.handleRequest(event, context));

        assertEquals("Failed to send authorisation request", exception.getMessage());
        assertEquals(ioException, exception.getCause());

        verify(httpClient, times(1)).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient, times(1)).send(any(), any());
    }

    @Test
    void publishRecordThrowsExceptionIfGroRecordRequestsFails() throws IOException, InterruptedException {
        when(httpClient.send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("httpBody");
        when(objectMapper.readValue("httpBody", CognitoTokenResponse.class))
            .thenReturn(new CognitoTokenResponse(
                "accessToken",
                "expiresIn",
                "tokenType"
            ));
        when(objectMapper.writeValueAsString(event)).thenReturn(eventAsString);
        var ioException = new IOException();
        when(httpClient.send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString())).thenThrow(ioException);

        var exception = assertThrows(GroApiCallException.class, () -> underTest.handleRequest(event, context));

        assertEquals("Failed to send GRO record request", exception.getMessage());
        assertEquals(ioException, exception.getCause());

        verify(httpClient, times(1)).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient, times(1)).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void publishRecordThrowsMappingExceptionIfGroRecordFailsToMap() throws IOException, InterruptedException {
        when(httpClient.send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("httpBody");
        when(objectMapper.readValue("httpBody", CognitoTokenResponse.class))
            .thenReturn(new CognitoTokenResponse(
                "accessToken",
                "expiresIn",
                "tokenType"
            ));
        var jsonProcessingException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(event)).thenThrow(jsonProcessingException);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        assertEquals(jsonProcessingException, exception.getCause());

        verify(httpClient, times(1)).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient, never()).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }
}
