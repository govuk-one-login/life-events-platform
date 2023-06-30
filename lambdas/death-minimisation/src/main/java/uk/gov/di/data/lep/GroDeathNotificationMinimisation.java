package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import uk.gov.di.data.lep.classes.GroDeathEventDetails;
import uk.gov.di.data.lep.classes.GroDeathEventNotification;
import uk.gov.di.data.lep.classes.GroDeathEventEnrichedData;
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
            map(enrichedData.sex(), EnrichmentField.SEX),
            map(enrichedData.dateOfBirth(), EnrichmentField.DATE_OF_BIRTH),
            map(enrichedData.dateOfDeath(), EnrichmentField.DATE_OF_DEATH),
            map(enrichedData.registrationId(), EnrichmentField.REGISTRATION_ID),
            map(enrichedData.eventTime(), EnrichmentField.EVENT_TIME),
            map(enrichedData.verificationLevel(), EnrichmentField.VERIFICATION_LEVEL),
            map(enrichedData.partialMonthOfDeath(), EnrichmentField.PARTIAL_MONTH_OF_DEATH),
            map(enrichedData.partialYearOfDeath(), EnrichmentField.PARTIAL_YEAR_OF_DEATH),
            map(enrichedData.forenames(), EnrichmentField.FORENAMES),
            map(enrichedData.surname(), EnrichmentField.SURNAME),
            map(enrichedData.maidenSurname(), EnrichmentField.MAIDEN_SURNAME),
            map(enrichedData.addressLine1(), EnrichmentField.ADDRESS_LINE_1),
            map(enrichedData.addressLine2(), EnrichmentField.ADDRESS_LINE_2),
            map(enrichedData.addressLine3(), EnrichmentField.ADDRESS_LINE_3),
            map(enrichedData.addressLine4(), EnrichmentField.ADDRESS_LINE_4),
            map(enrichedData.postcode(), EnrichmentField.POSTCODE)
        );

        return eventNotification;
    }

    public <T> T map(T field, EnrichmentField enrichmentField) {
        return enrichmentFields.contains(enrichmentField) ? field : null;
    }
}
