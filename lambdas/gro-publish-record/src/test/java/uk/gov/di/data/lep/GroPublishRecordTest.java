package uk.gov.di.data.lep;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.exceptions.GroApiCallException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithHeaders;
import uk.gov.di.data.lep.library.dto.RecordLocation;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.GroPublishRecordAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.audit.GroPublishRecordAuditExtensions;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Hasher;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroPublishRecordTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final HttpClient httpClient = mock(HttpClient.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final RecordLocation recordLocation = mock(RecordLocation.class);
    private static final GroPublishRecord underTest = new GroPublishRecord(awsService, config, objectMapper);
    private static final GroJsonRecordWithHeaders event = new GroJsonRecordWithHeaders(new GroJsonRecordBuilder().build(), "accessToken", "correlationID");
    private static final InputStream eventAsInputStream = mock(InputStream.class);
    private static final String eventAsString = "EventInStringRepresentation";
    private static final String recordFromBucket = "RecordFromBucket";
    private static final HttpRequest expectedGroRecordRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://lifeEventsPlatformDomain/events/deathNotification"))
        .header("Authorization", "accessToken")
        .header("CorrelationID", "correlationID")
        .POST(HttpRequest.BodyPublishers.ofString(eventAsString))
        .build();

    @BeforeAll
    static void setup() {
        when(config.getLifeEventsPlatformDomain()).thenReturn("lifeEventsPlatformDomain");
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
        clearInvocations(config);
        reset(httpClient);
        reset(objectMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new GroPublishRecord();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.close();
        }
    }

    @Test
    void publishRecordSendsGroRecordRequestsAndReturnsNull() throws IOException, InterruptedException {
        var httpClientMock = mockStatic(HttpClient.class);
        var httpResponseMock = mock(HttpResponse.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(objectMapper.readValue(eventAsInputStream, RecordLocation.class)).thenReturn(recordLocation);
        when(awsService.getFromBucket(recordLocation.jsonBucket(), recordLocation.jsonKey())).thenReturn(recordFromBucket);
        when(objectMapper.readValue(recordFromBucket, GroJsonRecordWithHeaders.class)).thenReturn(event);
        when(objectMapper.writeValueAsString(event.groJsonRecord())).thenReturn(eventAsString);
        when(httpClient.send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponseMock);
        when(httpResponseMock.statusCode()).thenReturn(201);

        underTest.handleRequest(eventAsInputStream, null, null);

        verify(httpClient).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());

        httpClientMock.close();
    }

    @Test
    void publishRecordThrowsExceptionIfGroRecordRequestsFails() throws IOException, InterruptedException {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(objectMapper.readValue(eventAsInputStream, RecordLocation.class)).thenReturn(recordLocation);
        when(awsService.getFromBucket(recordLocation.jsonBucket(), recordLocation.jsonKey())).thenReturn(recordFromBucket);
        when(objectMapper.readValue(recordFromBucket, GroJsonRecordWithHeaders.class)).thenReturn(event);
        when(objectMapper.writeValueAsString(event.groJsonRecord())).thenReturn(eventAsString);
        var ioException = new IOException();
        when(httpClient.send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString())).thenThrow(ioException);

        var exception = assertThrows(GroApiCallException.class, () -> underTest.handleRequest(eventAsInputStream, null, null));

        assertEquals("Failed to send GRO record request", exception.getMessage());
        assertEquals(ioException, exception.getCause());

        verify(httpClient, times(1)).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());

        httpClientMock.close();
    }

    @Test
    void publishRecordThrowsMappingExceptionIfGroRecordFailsToMap() throws IOException, InterruptedException {
        when(objectMapper.readValue(eventAsInputStream, RecordLocation.class)).thenReturn(recordLocation);
        when(awsService.getFromBucket(recordLocation.jsonBucket(), recordLocation.jsonKey())).thenReturn(recordFromBucket);
        when(objectMapper.readValue(recordFromBucket, GroJsonRecordWithHeaders.class)).thenReturn(event);
        var jsonProcessingException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(event.groJsonRecord())).thenThrow(jsonProcessingException);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(eventAsInputStream, null, null));

        assertEquals(jsonProcessingException, exception.getCause());

        verify(httpClient, never()).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void publishRecordThrowsMappingExceptionIfInputStreamFailsToMap() throws IOException, InterruptedException {
        var ioException = mock(IOException.class);
        when(objectMapper.readValue(eventAsInputStream, RecordLocation.class)).thenThrow(ioException);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(eventAsInputStream, null, null));

        assertEquals(ioException, exception.getCause());

        verify(httpClient, never()).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void groPublishRecordAuditsData() throws IOException, InterruptedException {
        var httpClientMock = mockStatic(HttpClient.class);
        var httpResponseMock = mock(HttpResponse.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(objectMapper.readValue(eventAsInputStream, RecordLocation.class)).thenReturn(recordLocation);
        when(awsService.getFromBucket(recordLocation.jsonBucket(), recordLocation.jsonKey())).thenReturn(recordFromBucket);
        when(objectMapper.readValue(recordFromBucket, GroJsonRecordWithHeaders.class)).thenReturn(event);
        when(objectMapper.writeValueAsString(event.groJsonRecord())).thenReturn(eventAsString);
        when(httpClient.send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(httpResponseMock);
        when(httpResponseMock.statusCode()).thenReturn(201);

        var groPublishRecordAudit = new GroPublishRecordAudit(
            new GroPublishRecordAuditExtensions(Hasher.hash("Gro publish record"), "correlationID")
        );
        when(objectMapper.writeValueAsString(event.groJsonRecord())).thenReturn("Gro publish record");
        when(objectMapper.writeValueAsString(groPublishRecordAudit)).thenReturn("Audit data");

        underTest.handleRequest(eventAsInputStream, null, null);

        verify(objectMapper).writeValueAsString(groPublishRecordAudit);
        verify(awsService).putOnAuditQueue("Audit data");
    }
}
