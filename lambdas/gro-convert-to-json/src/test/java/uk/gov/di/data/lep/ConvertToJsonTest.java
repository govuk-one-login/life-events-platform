package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventBucket;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventDetail;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventObject;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.services.AwsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConvertToJsonTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final ConvertToJson underTest = new ConvertToJson(awsService, config);
    private static final Context context = mock(Context.class);
    private static final S3ObjectCreatedNotificationEvent event =  new S3ObjectCreatedNotificationEvent(
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
    private static final String mockS3objectResponseMultipleRecords =
        "<DeathRegistrationGroup>" +
            "<DeathRegistration>" +
            "<RegistrationID>1</RegistrationID>" +
            "</DeathRegistration>" +
            "<DeathRegistration>" +
            "<RegistrationID>2</RegistrationID>" +
            "</DeathRegistration>" +
            "</DeathRegistrationGroup>";
    private static final String mockS3objectResponseOneRecord =
        "<DeathRegistrationGroup>" +
            "<DeathRegistration>" +
            "<RegistrationID>1</RegistrationID>" +
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
        new ConvertToJson();
        assertEquals(1, awsService.constructed().size());
        assertEquals(1, config.constructed().size());
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
            eq("[{\"RegistrationID\":1},{\"RegistrationID\":2}]")
        );
    }
    @Test
    void convertToJsonUploadsOneRecordToS3() {
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponseOneRecord);
        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            eq("[{\"RegistrationID\":1}]")
        );
    }
}
