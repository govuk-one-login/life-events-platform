package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import uk.gov.di.data.lep.classes.GroDeathEventDetails;
import uk.gov.di.data.lep.classes.GroDeathEventNotification;
import uk.gov.di.data.lep.classes.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.enums.EnrichmentField;
import uk.gov.di.data.lep.enums.EventType;

import java.util.Arrays;
import java.util.UUID;

public class GroDeathNotificationMinimisation implements RequestHandler<GroDeathEventEnrichedData, GroDeathEventNotification> {
    @Override
    public GroDeathEventNotification handleRequest(GroDeathEventEnrichedData enrichedData, Context context) {


        // Set up general (~meta) output data
        GroDeathEventNotification eventNotification = new GroDeathEventNotification();
        eventNotification.eventId = UUID.randomUUID();
        eventNotification.eventType = EventType.DEATH_NOTIFICATION;
        eventNotification.sourceId = enrichedData.sourceId;
        eventNotification.dataIncluded = true;
        eventNotification.enrichmentFields = Config.enrichmentFields;

        var eventDetails = new GroDeathEventDetails();

        for (var enrichmentField : Config.enrichmentFields) {
            if (enrichmentField == EnrichmentField.SOURCE_ID) {
                eventDetails.sourceId = enrichedData.sourceId;
            }
            if (enrichmentField == EnrichmentField.SEX) {
                eventDetails.sex = enrichedData.sex;
            }
            if (enrichmentField == EnrichmentField.DATE_OF_BIRTH) {
                eventDetails.dateOfBirth = enrichedData.dateOfBirth;
            }
            if (enrichmentField == EnrichmentField.DATE_OF_DEATH) {
                eventDetails.dateOfDeath = enrichedData.dateOfDeath;

            }
            if (enrichmentField == EnrichmentField.REGISTRATION_ID) {
                eventDetails.registrationId = enrichedData.registrationId;
            }
            if (enrichmentField == EnrichmentField.EVENT_TIME) {
                eventDetails.eventTime = enrichedData.eventTime;
            }
            if (enrichmentField == EnrichmentField.VERIFICATION_LEVEL) {
                eventDetails.verificationLevel = enrichedData.verificationLevel;
            }
            if (enrichmentField == EnrichmentField.PARTIAL_MONTH_OF_DEATH) {
                eventDetails.partialMonthOfDeath = enrichedData.partialMonthOfDeath;
            }
            if (enrichmentField == EnrichmentField.PARTIAL_YEAR_OF_DEATH) {
                eventDetails.partialYearOfDeath = enrichedData.partialYearOfDeath;
            }
            if (enrichmentField == EnrichmentField.FORENAMES) {
                eventDetails.forenames = enrichedData.forenames;
            }
            if (enrichmentField == EnrichmentField.SURNAME) {
                eventDetails.surname = enrichedData.surname;
            }
            if (enrichmentField == EnrichmentField.MAIDEN_SURNAME) {
                eventDetails.maidenSurname = enrichedData.maidenSurname;
            }
            if (enrichmentField == EnrichmentField.ADDRESS_LINE_1) {
                eventDetails.addressLine1 = enrichedData.addressLine1;
            }
            if (enrichmentField == EnrichmentField.ADDRESS_LINE_2) {
                eventDetails.addressLine2 = enrichedData.addressLine2;
            }
            if (enrichmentField == EnrichmentField.ADDRESS_LINE_3) {
                eventDetails.addressLine3 = enrichedData.addressLine3;
            }
            if (enrichmentField == EnrichmentField.ADDRESS_LINE_4) {
                eventDetails.addressLine4 = enrichedData.addressLine4;
            }
            if (enrichmentField == EnrichmentField.POSTCODE) {
                eventDetails.postcode = enrichedData.postcode;
            }
        }

        eventNotification.eventDetails = eventDetails;

        return eventNotification;
    }
}
