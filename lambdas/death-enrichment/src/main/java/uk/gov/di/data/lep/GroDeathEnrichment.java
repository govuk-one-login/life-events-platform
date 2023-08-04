package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;
import uk.gov.di.data.lep.library.dto.GroDeathEventDetails;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.GroSex;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GroDeathEnrichment
    extends LambdaHandler<GroDeathEventEnrichedData>
    implements RequestHandler<SQSEvent, GroDeathEventEnrichedData> {

    public GroDeathEnrichment() {
    }

    public GroDeathEnrichment(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroDeathEventEnrichedData handleRequest(SQSEvent sqsEvent, Context context) {
        try {
            var sqsMessage = sqsEvent.getRecords().get(0);
            var baseData = objectMapper.readValue(sqsMessage.getBody(), GroDeathEventBaseData.class);
            var enrichedData = enrichData(baseData);
            return publish(enrichedData);
        } catch (JsonProcessingException e) {
            logger.error("Failed to enrich request due to mapping error");
            throw new MappingException(e);
        }
    }

    @Tracing
    private GroDeathEventEnrichedData enrichData(GroDeathEventBaseData baseData) {
        logger.info("Enriching data (sourceId: {})", baseData.sourceId());
        var enrichmentData = getEnrichmentData();

        return new GroDeathEventEnrichedData(
            baseData.sourceId(),
            enrichmentData.sex(),
            enrichmentData.dateOfBirth(),
            enrichmentData.dateOfDeath(),
            enrichmentData.registrationId(),
            enrichmentData.eventTime(),
            enrichmentData.verificationLevel(),
            enrichmentData.partialMonthOfDeath(),
            enrichmentData.partialYearOfDeath(),
            enrichmentData.forenames(),
            enrichmentData.surname(),
            enrichmentData.maidenSurname(),
            enrichmentData.addressLine1(),
            enrichmentData.addressLine2(),
            enrichmentData.addressLine3(),
            enrichmentData.addressLine4(),
            enrichmentData.postcode()
        );
    }

    private GroDeathEventDetails getEnrichmentData() {
        return new GroDeathEventDetails(
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
    }
}
