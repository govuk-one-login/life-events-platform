package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathMinimisationAudit;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathMinimisationAuditExtensions;
import uk.gov.di.data.lep.library.dto.deathnotification.DeathNotificationSet;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.exceptions.MappingException;
import uk.gov.di.data.lep.library.services.AwsService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeathMinimisation
    extends LambdaHandler<String>
    implements RequestHandler<SQSEvent, String> {
    private final List<EnrichmentField> enrichmentFields = config.getEnrichmentFields();

    public DeathMinimisation() {
    }

    public DeathMinimisation(AwsService awsService, Config config, ObjectMapper objectMapper) {
        super(awsService, config, objectMapper);
    }

    @Override
    @Tracing
    @Logging(clearState = true)
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        try {
            var sqsMessage = sqsEvent.getRecords().get(0);
            var enrichedData = objectMapper.readValue(sqsMessage.getBody(), DeathNotificationSet.class);
            var filterProvider = generateEnrichmentFieldsFilterProvider();
            var minimisedData = objectMapper
                .writer(filterProvider)
                .writeValueAsString(enrichedData);
            var publishedData = publish(minimisedData);

            var auditData = generateAuditData(minimisedData);
            addAuditDataToQueue(auditData);

            return publishedData;
        } catch (JsonProcessingException e) {
            logger.error("Failed to minimise request due to mapping error");
            throw new MappingException(e);
        }
    }

    @Tracing
    private SimpleFilterProvider generateEnrichmentFieldsFilterProvider() {
        var fieldsToIgnore = new ArrayList<>(List.of(EnrichmentField.values()));
        fieldsToIgnore.removeAll(enrichmentFields);
        var ignoredFieldsSet = fieldsToIgnore.stream()
            .flatMap(e -> e.getFieldNames().stream())
            .collect(Collectors.toSet());

        return new SimpleFilterProvider()
            .addFilter("DeathNotificationSet", SimpleBeanPropertyFilter.serializeAllExcept(ignoredFieldsSet));
    }

    @Tracing
    private DeathMinimisationAudit generateAuditData(String minimisedData) {
        var auditDataExtensions = new DeathMinimisationAuditExtensions(config.getTargetQueue(), minimisedData.hashCode());
        return new DeathMinimisationAudit(auditDataExtensions);
    }
}
