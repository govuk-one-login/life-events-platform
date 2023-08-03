package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConvertToJsonTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final ConvertToJson underTest = new ConvertToJson(awsService, config);
    private static final Context context = mock(Context.class);
    private static S3ObjectCreatedNotificationEvent event;
    private static final String mockS3objectResponse =
        "<DeathRegistrationGroup>" +
            "<DeathRegistration>" +
            "<RegistrationID>1</RegistrationID>" +
            "</DeathRegistration>" +
            "<DeathRegistration>" +
            "<RegistrationID>2</RegistrationID>" +
            "</DeathRegistration>" +
            "</DeathRegistrationGroup>";

    @BeforeAll
    static void setup() throws JsonProcessingException {
        when(config.getGroRecordsBucketName()).thenReturn("JsonBucketName");
        when(awsService.getFromBucket(anyString(), anyString())).thenReturn(mockS3objectResponse);

        event = new S3ObjectCreatedNotificationEvent(
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
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
    }

    @Test
    void convertToJsonReturnsBucketsDetails() {
        var result = underTest.handleRequest(event, context);

        assertEquals("XMLBucketName", result.xmlBucket());
        assertEquals("File.xml", result.xmlKey());
        assertEquals("JsonBucketName", result.jsonBucket());
        assertEquals(".json", result.jsonKey().substring(result.jsonKey().length() - 5));
    }

    @Test
    void convertToJsonUploadsToS3() {
        underTest.handleRequest(event, context);

        verify(awsService).putInBucket(
            eq("JsonBucketName"),
            anyString(),
            eq("[{\"RegistrationID\":1},{\"RegistrationID\":2}]")
        );
    }
}
