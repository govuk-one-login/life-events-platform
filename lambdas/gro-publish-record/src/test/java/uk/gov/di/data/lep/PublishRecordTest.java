package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

class PublishRecordTest {
    private static final PublishRecord underTest = new PublishRecord();
    private static final Context context = mock(Context.class);

//    @Test
//    void publishRecordReturnsNull() {
//        var event = new GroJsonRecord();
//
//        var result = underTest.handleRequest(event, context);
//
//        assertNull(result);
//    }
}
