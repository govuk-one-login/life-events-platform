package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEvent;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventBucket;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventDetail;
import uk.gov.di.data.lep.dto.S3ObjectCreatedNotificationEventObject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class SplitFileTest {
    private static final SplitFile underTest = new SplitFile();
    private static final Context context = mock(Context.class);

    @Test
    void splitFileReturnsNull() {
        var event = new S3ObjectCreatedNotificationEvent();
        event.detail = new S3ObjectCreatedNotificationEventDetail();
        event.detail.bucket = new S3ObjectCreatedNotificationEventBucket();
        event.detail.bucket.name = "Bucket Name";
        event.detail.object = new S3ObjectCreatedNotificationEventObject();
        event.detail.object.key = "File.txt";

        var result = underTest.handleRequest(event, context);

        assertNull(result);
    }
}
