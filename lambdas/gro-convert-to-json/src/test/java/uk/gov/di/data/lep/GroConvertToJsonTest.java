package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.GuidScrubber;
import org.approvaltests.scrubbers.RegExScrubber;
import org.approvaltests.scrubbers.Scrubbers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import uk.gov.di.data.lep.dto.CognitoTokenResponse;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventBucket;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventDetail;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventObject;
import uk.gov.di.data.lep.exceptions.AuthException;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.gro.DeathRegistrationGroup;
import uk.gov.di.data.lep.library.dto.gro.audit.GroConvertToJsonAudit;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Hasher;
import uk.gov.di.data.lep.library.services.Mapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroConvertToJsonTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final HttpClient httpClient = mock(HttpClient.class);
    private static final HttpResponse<String> httpResponse = mock(HttpResponse.class);
    private static final GroConvertToJson underTest = new GroConvertToJson(
        awsService,
        config,
        Mapper.objectMapper(),
        Mapper.xmlMapper()
    );
    private static final Context context = mock(Context.class);
    private static final S3ObjectCreatedNotificationEvent event = new S3ObjectCreatedNotificationEvent(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        List.of(),
        new S3ObjectCreatedNotificationEventDetail(
            "",
            new S3ObjectCreatedNotificationEventBucket(
                "XMLBucketName"
            ),
            new S3ObjectCreatedNotificationEventObject(
                "File.xml",
                0,
                "",
                ""
            ),
            "",
            "",
            "",
            ""
        )
    );
    private static final String mockS3objectResponseOneRecord = """
        <DeathRegistrationGroup xmlns="http://www.ons.gov.uk/gro/OGDDeathExtractDWP" xmlns:ns1="http://www.govtalk.gov.uk/people/PersonDescriptives" xmlns:ns2="http://www.govtalk.gov.uk/people/AddressAndPersonalDetails" xmlns:ns3="http://www.ons.gov.uk/gro/people/GROAddressDescriptives" xmlns:ns4="http://www.ons.gov.uk/gro/people/GROPersonDescriptives" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ons.gov.uk/gro/OGDDeathExtractDWP .\\OGDDeathExtractDWP-v1-1.xsd">
            <DeathRegistration>
                <RegistrationID>1</RegistrationID>
                <DeceasedName>
                    <ns4:PersonGivenName>ERICA</ns4:PersonGivenName>
                    <ns4:PersonGivenName>CHRISTINA</ns4:PersonGivenName>
                    <ns4:PersonFamilyName>BLOGG</ns4:PersonFamilyName>
                </DeceasedName>
                <DeceasedGender>2</DeceasedGender>
                <DeceasedAddress>
                    <ns3:Line>263 Ave Maria Lane</ns3:Line>
                    <ns3:Line>Bourton-on-the-hill</ns3:Line>
                    <ns3:Postcode>BT62 4HL</ns3:Postcode>
                </DeceasedAddress>
            </DeathRegistration>
            <RecordCount>1</RecordCount>
        </DeathRegistrationGroup>
        """;
    private static final String mockS3objectResponseMultipleRecords = """
        <DeathRegistrationGroup xmlns="http://www.ons.gov.uk/gro/OGDDeathExtractDWP" xmlns:ns1="http://www.govtalk.gov.uk/people/PersonDescriptives" xmlns:ns2="http://www.govtalk.gov.uk/people/AddressAndPersonalDetails" xmlns:ns3="http://www.ons.gov.uk/gro/people/GROAddressDescriptives" xmlns:ns4="http://www.ons.gov.uk/gro/people/GROPersonDescriptives" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ons.gov.uk/gro/OGDDeathExtractDWP .\\OGDDeathExtractDWP-v1-1.xsd">
            <DeathRegistration>
                <RegistrationID>1</RegistrationID>
                <DeceasedName>
                    <ns4:PersonGivenName>ERICA</ns4:PersonGivenName>
                    <ns4:PersonGivenName>CHRISTINA</ns4:PersonGivenName>
                    <ns4:PersonFamilyName>BLOGG</ns4:PersonFamilyName>
                </DeceasedName>
                <DeceasedGender>2</DeceasedGender>
                <DeceasedAddress>
                    <ns3:Line>263 Ave Maria Lane</ns3:Line>
                    <ns3:Line>Bourton-on-the-hill</ns3:Line>
                    <ns3:Postcode>BT62 4HL</ns3:Postcode>
                </DeceasedAddress>
            </DeathRegistration>
            <DeathRegistration>
                <RegistrationID>2</RegistrationID>
                <DeceasedName>
                    <ns4:PersonGivenName>BOB</ns4:PersonGivenName>
                </DeceasedName>
                <DeceasedGender>1</DeceasedGender>
                <DeceasedBirthDate>
                    <ns1:PersonBirthDate>1958-06-06</ns1:PersonBirthDate>
                    <ns1:VerificationLevel>02</ns1:VerificationLevel>
                </DeceasedBirthDate>
            </DeathRegistration>
            <RecordCount>2</RecordCount>
        </DeathRegistrationGroup>
        """;
    private static final String mockS3objectResponseNoRecords = """
        <DeathRegistrationGroup xmlns="http://www.ons.gov.uk/gro/OGDDeathExtractDWP" xmlns:ns1="http://www.govtalk.gov.uk/people/PersonDescriptives" xmlns:ns2="http://www.govtalk.gov.uk/people/AddressAndPersonalDetails" xmlns:ns3="http://www.ons.gov.uk/gro/people/GROAddressDescriptives" xmlns:ns4="http://www.ons.gov.uk/gro/people/GROPersonDescriptives" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ons.gov.uk/gro/OGDDeathExtractDWP .\\OGDDeathExtractDWP-v1-1.xsd">
            <RecordCount>0</RecordCount>
        </DeathRegistrationGroup>
        """;
    private static final String mockS3objectResponseExpectedRecordsButFoundNone = """
        <DeathRegistrationGroup xmlns="http://www.ons.gov.uk/gro/OGDDeathExtractDWP" xmlns:ns1="http://www.govtalk.gov.uk/people/PersonDescriptives" xmlns:ns2="http://www.govtalk.gov.uk/people/AddressAndPersonalDetails" xmlns:ns3="http://www.ons.gov.uk/gro/people/GROAddressDescriptives" xmlns:ns4="http://www.ons.gov.uk/gro/people/GROPersonDescriptives" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ons.gov.uk/gro/OGDDeathExtractDWP .\\OGDDeathExtractDWP-v1-1.xsd">
            <RecordCount>6</RecordCount>
        </DeathRegistrationGroup>
        """;
    private static final String mockS3objectResponseExpectedRecordCountNotMatchingFoundRecordCount = """
        <DeathRegistrationGroup xmlns="http://www.ons.gov.uk/gro/OGDDeathExtractDWP" xmlns:ns1="http://www.govtalk.gov.uk/people/PersonDescriptives" xmlns:ns2="http://www.govtalk.gov.uk/people/AddressAndPersonalDetails" xmlns:ns3="http://www.ons.gov.uk/gro/people/GROAddressDescriptives" xmlns:ns4="http://www.ons.gov.uk/gro/people/GROPersonDescriptives" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.ons.gov.uk/gro/OGDDeathExtractDWP .\\OGDDeathExtractDWP-v1-1.xsd">
            <DeathRegistration>
                <RegistrationID>1</RegistrationID>
                <DeceasedName>
                    <ns4:PersonGivenName>ERICA</ns4:PersonGivenName>
                    <ns4:PersonGivenName>CHRISTINA</ns4:PersonGivenName>
                    <ns4:PersonFamilyName>BLOGG</ns4:PersonFamilyName>
                </DeceasedName>
                <DeceasedGender>2</DeceasedGender>
                <DeceasedAddress>
                    <ns3:Line>263 Ave Maria Lane</ns3:Line>
                    <ns3:Line>Bourton-on-the-hill</ns3:Line>
                    <ns3:Postcode>BT62 4HL</ns3:Postcode>
                </DeceasedAddress>
            </DeathRegistration>
            <DeathRegistration>
                <RegistrationID>2</RegistrationID>
                <DeceasedName>
                    <ns4:PersonGivenName>BOB</ns4:PersonGivenName>
                </DeceasedName>
                <DeceasedGender>1</DeceasedGender>
                <DeceasedBirthDate>
                    <ns1:PersonBirthDate>1958-06-06</ns1:PersonBirthDate>
                    <ns1:VerificationLevel>02</ns1:VerificationLevel>
                </DeceasedBirthDate>
            </DeathRegistration>
            <RecordCount>5</RecordCount>
        </DeathRegistrationGroup>
        """;
    private static final String cognitoClientId = "cognitoClientId";
    private static final String cognitoClientSecret = "cognitoClientSecret";
    private static final String cognitoOauth2TokenUri = "https://cognitoDomainName.auth.awsRegion.amazoncognito.com/oauth2/token";
    private static final HttpRequest expectedAuthRequest = HttpRequest.newBuilder()
        .uri(URI.create(cognitoOauth2TokenUri))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(
            String.format("grant_type=client_credentials&client_id=%s&client_secret=%s", cognitoClientId, cognitoClientSecret))
        ).build();

    @BeforeAll
    static void setup() {
        when(awsService.getCognitoClientSecret(anyString(), anyString())).thenReturn(cognitoClientSecret);
        when(config.getCognitoClientId()).thenReturn(cognitoClientId);
        when(config.getCognitoOauth2TokenUri()).thenReturn(cognitoOauth2TokenUri);
        when(config.getGroRecordsBucketName()).thenReturn("JsonBucketName");
        when(config.getUserPoolId()).thenReturn("userPoolId");
        when(config.getAuditQueue()).thenReturn("auditQueue");
    }

    @BeforeEach
    void refreshSetup() throws IOException, InterruptedException {
        clearInvocations(awsService);
        reset(httpClient);
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("{\"access_token\":\"accessToken\",\"expires_in\":\"expiresIn\",\"token_type\":\"tokenType\"}");
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new GroConvertToJson();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.verify(Mapper::xmlMapper, times(1));
            mapper.close();
        }
    }

    @Test
    void convertToJsonSendsAuthRequest() throws IOException, InterruptedException {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseMultipleRecords);

        underTest.handleRequest(event, context);

        verify(httpClient).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());

        httpClientMock.close();
    }

    @Test
    void publishRecordDoesNotSendGroRecordRequestsIfNoAuthorisationToken() throws IOException, InterruptedException {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        var ioException = new IOException();
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(ioException);

        var exception = assertThrows(AuthException.class, () -> underTest.handleRequest(event, context));

        assertEquals("Failed to send authorisation request", exception.getMessage());
        assertEquals(ioException, exception.getCause());

        verify(httpClient, times(1)).send(expectedAuthRequest, HttpResponse.BodyHandlers.ofString());
        verify(httpClient, times(1)).send(any(), any());

        httpClientMock.close();
    }

    @Test
    void convertToJsonReturnsBucketsDetails() {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseMultipleRecords);

        var result = underTest.handleRequest(event, context);

        assertEquals("XMLBucketName", result.xmlBucket());
        assertEquals("File.xml", result.xmlKey());
        assertEquals("JsonBucketName", result.jsonBucket());
        assertEquals(".json", result.jsonKey().substring(result.jsonKey().length() - 5));

        httpClientMock.close();
    }

    @Test
    void convertToJsonUploadsMultipleRecordsToS3() {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseMultipleRecords);

        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            matches(
                "\"RegistrationID\":1.*" +
                "\"PersonGivenName\":.*[\"ERICA\",\"CHRISTINA\"].*" +
                "\"PersonFamilyName\":\"BLOGG\".*" +
                "\"DeceasedGender\":2.*" +
                "\"RegistrationID\":2.*" +
                "\"PersonGivenName\":\\[\"BOB\"\\].*" +
                "\"DeceasedGender\":1.*" +
                "\"DeceasedBirthDate\":\\{\"PersonBirthDate\":\"1958-06-06\",\"VerificationLevel\":\"02\"\\}"
            )
        );

        httpClientMock.close();
    }

    @Test
    void convertToJsonUploadsOneRecordToS3() {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseOneRecord);

        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            matches("\"RegistrationID\":1.*" +
                    "\"PersonGivenName\":\\[\"ERICA\",\"CHRISTINA\"\\].*" +
                    "\"PersonFamilyName\":\"BLOGG\".*" +
                    "\"DeceasedGender\":2.*" +
                    "\"DeceasedAddress\":.*[\"263 Ave Maria Lane\",\"Bourton-on-the-hill\"]")
        );

        httpClientMock.close();
    }

    @Test
    void convertToJsonThrowsMappingException() throws JsonProcessingException {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        var xmlMapper = mock(XmlMapper.class);
        var underTest = new GroConvertToJson(awsService, config, Mapper.objectMapper(), xmlMapper);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseOneRecord);
        when(xmlMapper.readValue(mockS3objectResponseOneRecord, DeathRegistrationGroup.class)).thenThrow(JsonProcessingException.class);

        assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        verify(awsService, never()).putInBucket(any(), any(), any());

        httpClientMock.close();
    }

    private static Stream<Arguments> groFiles() {
        return Stream.of(
            Arguments.of(mockS3objectResponseOneRecord, "mockS3objectResponseOneRecord"),
            Arguments.of(mockS3objectResponseMultipleRecords, "mockS3objectResponseMultipleRecords")
        );
    }

    @Test
    void convertToJsonThrowsMappingExceptionForRegistrationGroupsWithNoRecords() {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseNoRecords);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        assertThat(exception.getMessage()).contains("File contains no registration records");
        verify(awsService, never()).putInBucket(any(), any(), any());

        httpClientMock.close();
    }

    @Test
    void convertToJsonThrowsMappingExceptionWhenExpectedRecordsButFoundNone() {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseExpectedRecordsButFoundNone);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        assertThat(exception.getMessage()).contains("Expected 6 records but none were found");
        verify(awsService, never()).putInBucket(any(), any(), any());

        httpClientMock.close();
    }

    @Test
    void convertToJsonThrowsMappingExceptionWhenExpectedRecordCountDoesntMatchFoundRecordCount() {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseExpectedRecordCountNotMatchingFoundRecordCount);

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        assertThat(exception.getMessage()).contains("Expected 5 records but 2 were found");
        verify(awsService, never()).putInBucket(any(), any(), any());

        httpClientMock.close();
    }

    @ParameterizedTest
    @MethodSource("groFiles")
    void groConvertToJsonSnapshotTest(String groFile, String name) {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(groFile);

        underTest.handleRequest(event, context);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(awsService).putInBucket(anyString(), anyString(), captor.capture());

        httpClientMock.close();

        var options = new Options(Scrubbers.scrubAll(
            new GuidScrubber(),
            new RegExScrubber("\"iat\":\\d+,", n -> "\"iat\":" + n + ","))
        );
        Approvals.verify(captor.getValue(), Approvals.NAMES.withParameters(options, name));
    }

    @Test
    void convertToJsonAuditsData() throws JsonProcessingException {
        var httpClientMock = mockStatic(HttpClient.class);
        httpClientMock.when(HttpClient::newHttpClient).thenReturn(httpClient);

        var objectMapperMock = mock(ObjectMapper.class);
        var cognitoTokenResponse = mock(CognitoTokenResponse.class);
        when(objectMapperMock.readValue(anyString(), eq(CognitoTokenResponse.class))).thenReturn(cognitoTokenResponse);

        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseMultipleRecords);

        when(objectMapperMock.writeValueAsString(any(GroConvertToJsonAudit.class))).thenReturn("Audit data");

        var underTestWithMockObjectMapper = new GroConvertToJson(
            awsService,
            config,
            objectMapperMock,
            Mapper.xmlMapper()
        );
        underTestWithMockObjectMapper.handleRequest(event, context);

        ArgumentCaptor<GroConvertToJsonAudit> captor = ArgumentCaptor.forClass(GroConvertToJsonAudit.class);

        verify(objectMapperMock, times(2)).writeValueAsString(captor.capture());
        assertEquals(Hasher.hash(mockS3objectResponseMultipleRecords), captor.getValue().extensions().fileHash());
        verify(awsService, times(2)).putOnAuditQueue("Audit data");

        httpClientMock.close();
    }
}
