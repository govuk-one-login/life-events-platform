package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.dto.GroDeathEventNotification;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.GroDeathEventDetails;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.EventType;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.util.List;
import java.util.UUID;

public class GroDeathNotificationMinimisation
    extends LambdaHandler<GroDeathEventNotification>
    implements RequestHandler<SQSEvent, GroDeathEventNotification> {
    private final List<EnrichmentField> enrichmentFields = config.getEnrichmentFields();

    public GroDeathNotificationMinimisation() {
    }

    public GroDeathNotificationMinimisation(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public GroDeathEventNotification handleRequest(SQSEvent sqsEvent, Context context) {
        try {
            var sqsMessage = sqsEvent.getRecords().get(0);
            var enrichedData = objectMapper.readValue(sqsMessage.getBody(), GroDeathEventEnrichedData.class);
            var minimisedData = minimiseEnrichedData(enrichedData);
            return publish(minimisedData);
        } catch (JsonProcessingException e) {
            logger.error("Failed to minimise request due to mapping error");
            throw new MappingException(e);
        }
    }

    @Tracing
    private GroDeathEventNotification minimiseEnrichedData(GroDeathEventEnrichedData enrichedData) {
        logger.info("Minimising enriched data (sourceId: {})", enrichedData.sourceId());

        return new GroDeathEventNotification(
            UUID.randomUUID(),
            EventType.DEATH_NOTIFICATION,
            enrichedData.sourceId(),
            enrichmentFields,
            new GroDeathEventDetails(
                minimiseIfRequired(enrichedData.sex(), EnrichmentField.SEX),
                minimiseIfRequired(enrichedData.dateOfBirth(), EnrichmentField.DATE_OF_BIRTH),
                minimiseIfRequired(enrichedData.dateOfDeath(), EnrichmentField.DATE_OF_DEATH),
                minimiseIfRequired(enrichedData.registrationId(), EnrichmentField.REGISTRATION_ID),
                minimiseIfRequired(enrichedData.eventTime(), EnrichmentField.EVENT_TIME),
                minimiseIfRequired(enrichedData.verificationLevel(), EnrichmentField.VERIFICATION_LEVEL),
                minimiseIfRequired(enrichedData.partialMonthOfDeath(), EnrichmentField.PARTIAL_MONTH_OF_DEATH),
                minimiseIfRequired(enrichedData.partialYearOfDeath(), EnrichmentField.PARTIAL_YEAR_OF_DEATH),
                minimiseIfRequired(enrichedData.forenames(), EnrichmentField.FORENAMES),
                minimiseIfRequired(enrichedData.surname(), EnrichmentField.SURNAME),
                minimiseIfRequired(enrichedData.maidenSurname(), EnrichmentField.MAIDEN_SURNAME),
                minimiseIfRequired(enrichedData.addressLine1(), EnrichmentField.ADDRESS_LINE_1),
                minimiseIfRequired(enrichedData.addressLine2(), EnrichmentField.ADDRESS_LINE_2),
                minimiseIfRequired(enrichedData.addressLine3(), EnrichmentField.ADDRESS_LINE_3),
                minimiseIfRequired(enrichedData.addressLine4(), EnrichmentField.ADDRESS_LINE_4),
                minimiseIfRequired(enrichedData.postcode(), EnrichmentField.POSTCODE)
            ));
    }

    private <T> T minimiseIfRequired(T field, EnrichmentField enrichmentField) {
        return enrichmentFields.contains(enrichmentField) ? field : null;
    }
}
