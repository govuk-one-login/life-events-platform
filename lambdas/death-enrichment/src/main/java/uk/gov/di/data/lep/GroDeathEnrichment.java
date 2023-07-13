package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import uk.gov.di.data.lep.library.LambdaHandler;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;
import uk.gov.di.data.lep.library.dto.GroDeathEventDetails;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.GroSex;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GroDeathEnrichment
    extends LambdaHandler<GroDeathEventEnrichedData>
    implements RequestHandler<GroDeathEventBaseData, GroDeathEventEnrichedData> {
    @Override
    public GroDeathEventEnrichedData handleRequest(GroDeathEventBaseData baseData, Context context) {
        logger = context.getLogger();
        var enrichedData = enrichData(baseData);
        return publish(enrichedData);
    }

    public GroDeathEventEnrichedData enrichData(GroDeathEventBaseData baseData) {
        logger.log("Enriching data (sourceId: " + baseData.sourceId() + ")");
        var enrichmentData = getEnrichmentData(baseData.sourceId());

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

    private GroDeathEventDetails getEnrichmentData(String sourceId) {
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
