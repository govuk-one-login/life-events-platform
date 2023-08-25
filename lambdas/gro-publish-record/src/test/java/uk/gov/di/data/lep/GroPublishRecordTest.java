package uk.gov.di.data.lep;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.exceptions.GroApiCallException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecordBuilder;
import uk.gov.di.data.lep.library.dto.GroJsonRecordWithAuth;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroPublishRecordTest {
    private static final Config config = mock(Config.class);
    private static final HttpClient httpClient = mock(HttpClient.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final GroPublishRecord underTest = new GroPublishRecord(config, objectMapper);
    private static final GroJsonRecordWithAuth event = new GroJsonRecordWithAuth(new GroJsonRecordBuilder().build(), "accessToken");
    private static final InputStream eventAsInputStream = mock(InputStream.class);
    private static final String eventAsString = "EventInStringRepresentation";
    private static final HttpRequest expectedGroRecordRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://lifeEventsPlatformDomain/events/deathNotification"))
        .header("Authorization", "accessToken")
        .POST(HttpRequest.BodyPublishers.ofString(eventAsString))
        .build();

    @BeforeAll
    static void setup() {
        when(config.getLifeEventsPlatformDomain()).thenReturn("lifeEventsPlatformDomain");
    }

    @BeforeEach
    void refreshSetup() {
        reset(httpClient);
        reset(objectMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new GroPublishRecord();
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.close();
        }
    }

    @Test
    void publishRecordSendsGroRecordRequestsAndReturnsNull() throws IOException, InterruptedException {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(objectMapper.readValue(eventAsInputStream, GroJsonRecordWithAuth.class)).thenReturn(event);
        when(objectMapper.writeValueAsString(event.groJsonRecord())).thenReturn(eventAsString);

        underTest.handleRequest(eventAsInputStream, null, null);

        verify(httpClient).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());

        httpClientMock.close();
    }

    @Test
    void publishRecordThrowsExceptionIfGroRecordRequestsFails() throws IOException, InterruptedException {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(objectMapper.readValue(eventAsInputStream, GroJsonRecordWithAuth.class)).thenReturn(event);
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
        when(objectMapper.readValue(eventAsInputStream, GroJsonRecordWithAuth.class)).thenReturn(event);
        var jsonProcessingException = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(event.groJsonRecord())).thenThrow(jsonProcessingException);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(eventAsInputStream, null, null));

        assertEquals(jsonProcessingException, exception.getCause());

        verify(httpClient, never()).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void publishRecordThrowsMappingExceptionIfInputStreamFailsToMap() throws IOException, InterruptedException {
        var ioException = mock(IOException.class);
        when(objectMapper.readValue(eventAsInputStream, GroJsonRecordWithAuth.class)).thenThrow(ioException);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(eventAsInputStream, null, null));

        assertEquals(ioException, exception.getCause());

        verify(httpClient, never()).send(expectedGroRecordRequest, HttpResponse.BodyHandlers.ofString());
    }
}
