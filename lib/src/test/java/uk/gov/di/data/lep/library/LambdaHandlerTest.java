package uk.gov.di.data.lep.library;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.GroSex;
import uk.gov.di.data.lep.library.services.AwsService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

public class LambdaHandlerTest {
    private static final LambdaHandler<GroDeathEventEnrichedData> underTest = new TestLambda();
    private static MockedStatic<Config> config;
    private static MockedStatic<AwsService> awsService;
    @Mock
    private static LambdaLogger logger = mock(LambdaLogger.class);

    @BeforeAll
    public static void setup() {
        config = mockStatic(Config.class);
        awsService = mockStatic(AwsService.class);
        underTest.logger = logger;
    }

    @BeforeEach
    public void refreshSetup() {
        clearInvocations(logger);
    }

    @AfterAll
    public static void tearDown() {
        config.close();
        awsService.close();
    }

    @Test
    public void publishPublishesMessageToQueue() {
        config.when(Config::getTargetQueue).thenReturn("targetQueueURL");

        var output = new GroDeathEventEnrichedData(
            "123a1234-a12b-12a1-a123-123456789012",
            GroSex.FEMALE,
            LocalDate.parse("1972-02-20"),
            LocalDate.parse("2021-12-31"),
            "123456789",
            LocalDateTime.parse("2022-01-05T12:03:52"),
            "1",
            "12",
            "2021",
            "Bob Burt",
            "Smith",
            "Jane",
            "888 Death House",
            "8 Death lane",
            "Deadington",
            "Deadshire",
            "XX1 1XX"
        );

        underTest.publish(output);

        verify(logger).log("Putting message on target queue: targetQueueURL");

        awsService.verify(() -> AwsService.putOnQueue(
            "{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\",\"sex\":\"FEMALE\",\"dateOfBirth\":\"1972-02-20\",\"dateOfDeath\":\"2021-12-31\",\"registrationId\":\"123456789\",\"eventTime\":\"2022-01-05T12:03:52.000\",\"verificationLevel\":\"1\",\"partialMonthOfDeath\":\"12\",\"partialYearOfDeath\":\"2021\",\"forenames\":\"Bob Burt\",\"surname\":\"Smith\",\"maidenSurname\":\"Jane\",\"addressLine1\":\"888 Death House\",\"addressLine2\":\"8 Death lane\",\"addressLine3\":\"Deadington\",\"addressLine4\":\"Deadshire\",\"postcode\":\"XX1 1XX\"}"
        ));
    }

    @Test
    public void publishPublishesMessageToTopic() {
        config.when(Config::getTargetTopic).thenReturn("targetTopicARN");

        var output = new GroDeathEventEnrichedData(
            "123a1234-a12b-12a1-a123-123456789012",
            GroSex.FEMALE,
            LocalDate.parse("1972-02-20"),
            LocalDate.parse("2021-12-31"),
            "123456789",
            LocalDateTime.parse("2022-01-05T12:03:52"),
            "1",
            "12",
            "2021",
            "Bob Burt",
            "Smith",
            "Jane",
            "888 Death House",
            "8 Death lane",
            "Deadington",
            "Deadshire",
            "XX1 1XX"
        );

        underTest.publish(output);

        verify(logger).log("Putting message on target topic: targetTopicARN");

        awsService.verify(() -> AwsService.putOnTopic(
            "{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\",\"sex\":\"FEMALE\",\"dateOfBirth\":\"1972-02-20\",\"dateOfDeath\":\"2021-12-31\",\"registrationId\":\"123456789\",\"eventTime\":\"2022-01-05T12:03:52.000\",\"verificationLevel\":\"1\",\"partialMonthOfDeath\":\"12\",\"partialYearOfDeath\":\"2021\",\"forenames\":\"Bob Burt\",\"surname\":\"Smith\",\"maidenSurname\":\"Jane\",\"addressLine1\":\"888 Death House\",\"addressLine2\":\"8 Death lane\",\"addressLine3\":\"Deadington\",\"addressLine4\":\"Deadshire\",\"postcode\":\"XX1 1XX\"}"
        ));
    }

    static class TestLambda extends LambdaHandler<GroDeathEventEnrichedData> {
    }
}
