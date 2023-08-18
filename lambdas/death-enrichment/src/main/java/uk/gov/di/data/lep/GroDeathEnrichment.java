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
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.dto.GroJsonRecord;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

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
            var baseData = objectMapper.readValue(sqsMessage.getBody(), GroJsonRecord.class);
            var enrichedData = enrichData(baseData);
            return publish(enrichedData);
        } catch (JsonProcessingException e) {
            logger.error("Failed to enrich request due to mapping error");
            throw new MappingException(e);
        }
    }

    @Tracing
    private GroDeathEventEnrichedData enrichData(GroJsonRecord baseData) {
        logger.info("Enriching and mapping data (sourceId: {})", baseData.registrationId());

        return new GroDeathEventEnrichedData(
            baseData.registrationId(),
            baseData.deceasedGender(),
            baseData.deceasedBirthDate().personBirthDate(),
            baseData.deceasedDeathDate().personDeathDate(),
            baseData.registrationId(),
            baseData.recordLockedDateTime() == null ? baseData.recordUpdateDateTime() : baseData.recordLockedDateTime(),
            baseData.partialMonthOfDeath(),
            baseData.partialYearOfDeath(),
            baseData.deceasedName().personGivenNames(),
            baseData.deceasedName().personFamilyName(),
            baseData.deceasedMaidenName(),
            baseData.deceasedAddress().flat(),
            baseData.deceasedAddress().building(),
            baseData.deceasedAddress().lines(),
            baseData.deceasedAddress().postcode()
        );
    }
}
