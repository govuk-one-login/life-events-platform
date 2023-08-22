import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.di.data.lep.GenerateGroXml;
import uk.gov.di.data.lep.dto.InsertDeathXmlRequest;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.DeathRegistrationGroup;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;
import uk.gov.di.data.lep.library.services.Mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateGroXmlTest {
    private static final AwsService awsService = mock(AwsService.class);
    private static final Config config = mock(Config.class);
    private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private static final XmlMapper xmlMapper = mock(XmlMapper.class);
    private static final Context context = mock(Context.class);
    private static final GenerateGroXml underTest = new GenerateGroXml(
        awsService,
        config,
        objectMapper,
        xmlMapper
    );
    private static final String bucketName = "groIngestionBucketName";

    @BeforeAll
    static void setup() {
        doNothing().when(awsService).putInBucket(any(), any(), any());
        when(config.getGroIngestionBucketName()).thenReturn(bucketName);
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(awsService);
        clearInvocations(config);
        reset(xmlMapper);
    }

    @Test
    void constructionCallsCorrectInstantiation() {
        try (var awsService = mockConstruction(AwsService.class);
             var config = mockConstruction(Config.class)) {
            var mapper = mockStatic(Mapper.class);
            new GenerateGroXml();
            assertEquals(1, awsService.constructed().size());
            assertEquals(1, config.constructed().size());
            mapper.verify(Mapper::objectMapper, times(1));
            mapper.verify(Mapper::xmlMapper, times(1));
            mapper.close();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 80})
    void generateDeathRegistrationGroupXmlGeneratesAGivenNumberOfRecords(int numberOfRecords) throws JsonProcessingException {
        var event = new InsertDeathXmlRequest("Test", numberOfRecords);
        when(xmlMapper.writeValueAsString(any())).thenReturn("XmlFile");

        underTest.handleRequest(event, context);

        verify(xmlMapper).writeValueAsString(argThat(a -> {
            var group = (DeathRegistrationGroup) a;
            return group.deathRegistrations().size() == numberOfRecords;
        }));
        verify(awsService).putInBucket(eq(bucketName), any(), eq("XmlFile"));
    }

    @Test
    void generateDeathRegistrationGroupXmlGenerates25RecordsIfNumberOfRecordsNotSpecified() throws JsonProcessingException {
        var event = new InsertDeathXmlRequest("Test", null);
        when(xmlMapper.writeValueAsString(any())).thenReturn("XmlFile");

        underTest.handleRequest(event, context);

        verify(xmlMapper).writeValueAsString(argThat(a -> {
            var group = (DeathRegistrationGroup) a;
            return group.deathRegistrations().size() == 25;
        }));
        verify(awsService).putInBucket(eq(bucketName), any(), eq("XmlFile"));
    }

    @Test
    void failingToWriteAsXmlThrowsError() throws JsonProcessingException {
        var event = new InsertDeathXmlRequest("Test", 10);
        when(xmlMapper.writeValueAsString(any())).thenThrow(mock(MappingException.class));

        var exception = assertThrows(MappingException.class, () -> underTest.handleRequest(event, context));

        assert (exception.toString().startsWith("Mock for MappingException"));
    }
}
