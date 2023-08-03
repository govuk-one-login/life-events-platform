package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class PublishRecordTest {
    private static final PublishRecord underTest = new PublishRecord();
    private static final Context context = mock(Context.class);

    @Test
    void publishRecordReturnsNull() {
        var event = new GroJsonRecord();

        var result = underTest.handleRequest(event, context);

        assertNull(result);
    }
}
