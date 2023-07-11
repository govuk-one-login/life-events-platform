package uk.gov.di.data.lep.library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.GroSex;
import uk.gov.di.data.lep.library.services.AwsService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mockStatic;

public class LambdaHandlerTest {
    private final LambdaHandler<GroDeathEventEnrichedData> underTest = new TestLambda();
    private static MockedStatic<Config> config;
    private static MockedStatic<AwsService> awsService;

    @BeforeEach
    public void setup() {
        config = mockStatic(Config.class);
        awsService = mockStatic(AwsService.class);
    }

    @AfterEach
    public void tearDown() {
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

        awsService.verify(() -> AwsService.putOnQueue(
            "{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\",\"sex\":\"FEMALE\",\"dateOfBirth\":\"20-02-1972\",\"dateOfDeath\":\"31-12-2021\",\"registrationId\":\"123456789\",\"eventTime\":\"05-01-2022 12:03:52\",\"verificationLevel\":\"1\",\"partialMonthOfDeath\":\"12\",\"partialYearOfDeath\":\"2021\",\"forenames\":\"Bob Burt\",\"surname\":\"Smith\",\"maidenSurname\":\"Jane\",\"addressLine1\":\"888 Death House\",\"addressLine2\":\"8 Death lane\",\"addressLine3\":\"Deadington\",\"addressLine4\":\"Deadshire\",\"postcode\":\"XX1 1XX\"}"
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

        awsService.verify(() -> AwsService.putOnTopic(
            "{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\",\"sex\":\"FEMALE\",\"dateOfBirth\":\"20-02-1972\",\"dateOfDeath\":\"31-12-2021\",\"registrationId\":\"123456789\",\"eventTime\":\"05-01-2022 12:03:52\",\"verificationLevel\":\"1\",\"partialMonthOfDeath\":\"12\",\"partialYearOfDeath\":\"2021\",\"forenames\":\"Bob Burt\",\"surname\":\"Smith\",\"maidenSurname\":\"Jane\",\"addressLine1\":\"888 Death House\",\"addressLine2\":\"8 Death lane\",\"addressLine3\":\"Deadington\",\"addressLine4\":\"Deadshire\",\"postcode\":\"XX1 1XX\"}"
        ));
    }

    static class TestLambda extends LambdaHandler<GroDeathEventEnrichedData> {
    }
}
