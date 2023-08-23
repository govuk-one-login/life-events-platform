package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventBucket;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventDetail;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventObject;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.DeathRegistrationGroup;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.List;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConvertToJsonTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
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
    private static final String mockS3objectResponseOneRecord =
        "<DeathRegistrationGroup>" +
            "<DeathRegistration>" +
            "<RegistrationID>1</RegistrationID>" +
            "<DeceasedName>" +
            "<PersonGivenName>ERICA</PersonGivenName>" +
            "<PersonGivenName>CHRISTINA</PersonGivenName>" +
            "</DeceasedName>" +
            "<DeceasedGender>2</DeceasedGender>" +
            "</DeathRegistration>" +
            "</DeathRegistrationGroup>";
    private static final String mockS3objectResponseMultipleRecords =
        "<DeathRegistrationGroup>" +
            "<DeathRegistration>" +
            "<RegistrationID>1</RegistrationID>" +
            "<DeceasedName>" +
            "<PersonGivenName>ERICA</PersonGivenName>" +
            "<PersonGivenName>CHRISTINA</PersonGivenName>" +
            "</DeceasedName>" +
            "<DeceasedGender>2</DeceasedGender>" +
            "</DeathRegistration>" +
            "<DeathRegistration>" +
            "<RegistrationID>2</RegistrationID>" +
            "<DeceasedName>" +
            "<PersonGivenName>BOB</PersonGivenName>" +
            "</DeceasedName>" +
            "<DeceasedGender>1</DeceasedGender>" +
            "<DeceasedBirthDate>" +
            "<PersonBirthDate>1958-06-06</PersonBirthDate>" +
            "<VerificationLevel>02</VerificationLevel>" +
            "</DeceasedBirthDate>" +
            "</DeathRegistration>" +
            "</DeathRegistrationGroup>";

    @BeforeAll
    static void setup() {
        when(config.getGroRecordsBucketName()).thenReturn("JsonBucketName");
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        var mapper = mockStatic(Mapper.class);
        new GroConvertToJson();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        mapper.verify(Mapper::objectMapper, times(1));
        mapper.verify(Mapper::xmlMapper, times(1));
        mapper.close();
    }

    @Test
    void convertToJsonReturnsBucketsDetails() {
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseMultipleRecords);

        var result = underTest.handleRequest(event, context);

        assertEquals("XMLBucketName", result.xmlBucket());
        assertEquals("File.xml", result.xmlKey());
        assertEquals("JsonBucketName", result.jsonBucket());
        assertEquals(".json", result.jsonKey().substring(result.jsonKey().length() - 5));
    }

    @Test
    void convertToJsonUploadsMultipleRecordsToS3() {
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseMultipleRecords);

        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            matches(
                "\"registrationID\":1.*" +
                    "\"personGivenNames\":.*\\[\"ERICA\",\"CHRISTINA\"\\].*" +
                    "\"deceasedGender\":2.*" +
                    "\"registrationID\":2.*" +
                    "\"personGivenNames\":\\[\"BOB\"\\].*" +
                    "\"deceasedGender\":1.*" +
                    "\"deceasedBirthDate\":\\{\"personBirthDate\":\"1958-06-06\",\"verificationLevel\":\"02\"\\}"
            )
        );
    }

    @Test
    void convertToJsonUploadsOneRecordToS3() {
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseOneRecord);

        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            matches("\"registrationID\":1.*" +
                "\"personGivenNames\":.*\\[\"ERICA\",\"CHRISTINA\"\\].*" +
                "\"deceasedGender\":2")
        );
    }

    @Test
    void convertToJsonThrowsMappingException() throws JsonProcessingException {
        var objectMapper = mock(ObjectMapper.class);
        var xmlMapper = mock(XmlMapper.class);
        var underTest = new GroConvertToJson(awsService, config, objectMapper, xmlMapper);
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseOneRecord);
        when(xmlMapper.readValue(mockS3objectResponseOneRecord, DeathRegistrationGroup.class)).thenThrow(JsonProcessingException.class);

        assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        verify(objectMapper, never()).writeValueAsString(any());
    }
}
