package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import uk.gov.di.data.lep.library.dto.GroDeathEventDetails;
import uk.gov.di.data.lep.dto.GroDeathEventNotification;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.EventType;
import uk.gov.di.data.lep.library.config.Config;

import java.util.List;
import java.util.UUID;

public class GroDeathNotificationMinimisation implements RequestHandler<GroDeathEventEnrichedData, GroDeathEventNotification> {
    private final List<EnrichmentField> enrichmentFields = Config.getEnrichmentFields();
    @Override
    public GroDeathEventNotification handleRequest(GroDeathEventEnrichedData enrichedData, Context context) {

        var eventNotification = new GroDeathEventNotification();
        eventNotification.eventId = UUID.randomUUID();
        eventNotification.eventType = EventType.DEATH_NOTIFICATION;
        eventNotification.sourceId = enrichedData.sourceId();
        eventNotification.enrichmentFields = enrichmentFields;

        eventNotification.eventDetails = new GroDeathEventDetails(
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
        );

        return eventNotification;
    }

    public <T> T minimiseIfRequired(T field, EnrichmentField enrichmentField) {
        return enrichmentFields.contains(enrichmentField) ? field : null;
    }
}
