package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.di.data.lep.classes.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.enums.GroSex;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MainTest {
    @Mock
    private Context context;

    private final GroDeathNotificationMinimisation underTest = new GroDeathNotificationMinimisation();

    @Test
    void handleRequestReturnsHello() {

        var enrichmentFieldsCsv = "sourceId,surname";
//        Mockito.when(System.getenv("ENRICHMENT_FIELDS")).thenReturn(enrichmentFieldsCsv);

        var enrichedData = new GroDeathEventEnrichedData(
                "123a1234-a12b-12a1-a123-123456789012",    // sourceId
                GroSex.FEMALE, // sex
                LocalDate.parse("1972-02-20"),   // dateOfBirth
                LocalDate.parse("2021-12-31"),   // dateOfDeath
                "123456789",   // registrationId
                LocalDate.parse("2022-01-05"),    // eventTime
                "1",    // verificationLevel
                "12",   // partialMonthOfDeath
                "2021", // partialYearOfDeath
                "Bob Burt", // forenames
                "Smith",    // surname
                "Jane", // maidenSurname
                "888 Death House",  // addressLine1
                "8 Death lane", // addressLine2
                "Deadington",   // addressLine3
                "Deadshire",    // addressLine4
                "XX1 1XX"  // postcode
                );


        var result = underTest.handleRequest(enrichedData, context);
        assertEquals("123a1234-a12b-12a1-a123-123456789012", result.eventDetails.sourceId);
        assertNull(result.eventDetails.forenames);
        assertEquals("Smith", result.eventDetails.surname);
    }
}
