package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventBucket;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventDetail;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventObject;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConvertToJsonTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final XmlMapper xmlMapper = mock(XmlMapper.class);
    private static ConvertToJson underTest = new ConvertToJson(awsService, config, objectMapper, xmlMapper);
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
            "</DeathRegistration>" +
            "</DeathRegistrationGroup>";
    private static final String mockS3objectResponseMultipleRecords =
        "<DeathRegistrationGroup>" +
            "<DeathRegistration>" +
            "<RegistrationID>1</RegistrationID>" +
            "</DeathRegistration>" +
            "<DeathRegistration>" +
            "<RegistrationID>2</RegistrationID>" +
            "<DeceasedGender>2</DeceasedGender>" +
            "<DeceasedBirthDate>" +
            "<PersonBirthDate>1958-06-06</PersonBirthDate>" +
            "<VerificationLevel>02</VerificationLevel>" +
            "</DeceasedBirthDate>" +
            "</DeathRegistration>" +
            "</DeathRegistrationGroup>";

    @BeforeAll
    static void setup() {
        when(config.getGroRecordsBucketName()).thenReturn("JsonBucketName");

        var mapper = new XmlMapper();
        mapper.registerModule(new JavaTimeModule());
        underTest = new ConvertToJson(awsService, config, objectMapper, mapper);
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
        clearInvocations(objectMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        var awsService = mockConstruction(AwsService.class);
        var config = mockConstruction(Config.class);
        var mapper = mockConstruction(Mapper.class);
        new ConvertToJson();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
        assertEquals(2, mapper.constructed().size());
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
    void convertToJsonUploadsMultipleRecordsToS3() throws JsonProcessingException {
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseMultipleRecords);
        when(objectMapper.writeValueAsString(any())).thenReturn(
            "\"[" +
                "{" +
                "\"RegistrationID\":1," +
                "\"RegistrationType\":0," +
                "\"RecordLockedDateTime\":null," +
                "\"RecordUpdateDateTime\":null," +
                "\"RecordUpdateReason\":0," +
                "\"DeceasedName\":null," +
                "\"DeceasedAliasName\":null," +
                "\"DeceasedAliasNameType\":null," +
                "\"DeceasedMaidenName\":null," +
                "\"DeceasedGender\":null," +
                "\"DeceasedDeathDate\":null," +
                "\"PartialMonthOfDeath\":0," +
                "\"PartialYearOfDeath\":0," +
                "\"QualifierText\":null," +
                "\"FreeFormatDeathDate\":null," +
                "\"DeceasedBirthDate\":null," +
                "\"PartialMonthOfBirth\":0," +
                "\"PartialYearOfBirth\":0," +
                "\"FreeFormatBirthDate\":null," +
                "\"DeceasedAddress\":null" +
                "}," +
                "{" +
                "\"RegistrationID\":2," +
                "\"RegistrationType\":0," +
                "\"RecordLockedDateTime\":null," +
                "\"RecordUpdateDateTime\":null," +
                "\"RecordUpdateReason\":0," +
                "\"DeceasedName\":null," +
                "\"DeceasedAliasName\":null," +
                "\"DeceasedAliasNameType\":null," +
                "\"DeceasedMaidenName\":null," +
                "\"DeceasedGender\":2," +
                "\"DeceasedDeathDate\":null," +
                "\"PartialMonthOfDeath\":0," +
                "\"PartialYearOfDeath\":0," +
                "\"QualifierText\":null," +
                "\"FreeFormatDeathDate\":null," +
                "\"DeceasedBirthDate\":{\"PersonBirthDate\":[1958,6,6]," +
                "\"VerificationLevel\":\"02\"}," +
                "\"PartialMonthOfBirth\":0," +
                "\"PartialYearOfBirth\":0," +
                "\"FreeFormatBirthDate\":null," +
                "\"DeceasedAddress\":null}" +
                "]\"");

        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            matches(".*\"RegistrationID\":1.*\"RegistrationID\":2.*")
        );
    }

    @Test
    void convertToJsonUploadsOneRecordToS3() throws JsonProcessingException {
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseOneRecord);
        when(objectMapper.writeValueAsString(any())).thenReturn(
            "\"[" +
                "{" +
                "\"RegistrationID\":1," +
                "\"RegistrationType\":0," +
                "\"RecordLockedDateTime\":null," +
                "\"RecordUpdateDateTime\":null," +
                "\"RecordUpdateReason\":0," +
                "\"DeceasedName\":null," +
                "\"DeceasedAliasName\":null," +
                "\"DeceasedAliasNameType\":null," +
                "\"DeceasedMaidenName\":null," +
                "\"DeceasedGender\":null," +
                "\"DeceasedDeathDate\":null," +
                "\"PartialMonthOfDeath\":0," +
                "\"PartialYearOfDeath\":0," +
                "\"QualifierText\":null," +
                "\"FreeFormatDeathDate\":null," +
                "\"DeceasedBirthDate\":null," +
                "\"PartialMonthOfBirth\":0," +
                "\"PartialYearOfBirth\":0," +
                "\"FreeFormatBirthDate\":null," +
                "\"DeceasedAddress\":null" +
                "}]\"");

        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            matches("\"RegistrationID\":1")
        );
    }
}
