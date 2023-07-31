package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventBucket;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventDetail;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventObject;
import uk.gov.di.data.lep.library.config.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConvertToJsonTest {
    private static final Config config = mock(Config.class);
    private static final ConvertToJson underTest = new ConvertToJson(config);
    private static final Context context = mock(Context.class);

    @Test
    void convertToJsonReturnsPayloadAndBucketDetails() {
        when(config.getGroRecordsBucketName()).thenReturn("BucketName");

        var event = new S3ObjectCreatedNotificationEvent();
        event.detail = new S3ObjectCreatedNotificationEventDetail();
        event.detail.bucket = new S3ObjectCreatedNotificationEventBucket();
        event.detail.bucket.name = "Bucket Name";
        event.detail.object = new S3ObjectCreatedNotificationEventObject();
        event.detail.object.key = "File.txt";

        var result = underTest.handleRequest(event, context);

        assertEquals(5, result.payload().size());
        assertEquals("BucketName", result.bucket());
        assertEquals(".json", result.key().substring(result.key().length() - 5));
    }
}
