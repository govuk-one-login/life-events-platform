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
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSetMapper;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.util.List;

public class DeathMinimisation
    extends LambdaHandler<DeathNotificationSet>
    implements RequestHandler<SQSEvent, DeathNotificationSet> {
    private final List<EnrichmentField> enrichmentFields = config.getEnrichmentFields();

    public DeathMinimisation() {
    }

    public DeathMinimisation(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public DeathNotificationSet handleRequest(SQSEvent sqsEvent, Context context) {
        try {
            var sqsMessage = sqsEvent.getRecords().get(0);
            var enrichedData = objectMapper.readValue(sqsMessage.getBody(), DeathNotificationSet.class);
            var minimisedData = minimiseEnrichedData(enrichedData);
            return publish(minimisedData);
        } catch (JsonProcessingException e) {
            logger.error("Failed to minimise request due to mapping error");
            throw new MappingException(e);
        }
    }

    @Tracing
    private DeathNotificationSet minimiseEnrichedData(DeathNotificationSet deathNotificationSet) {
        return DeathNotificationSetMapper.generateMinimisedDeathNotificationSet(deathNotificationSet, enrichmentFields);
    }
}
