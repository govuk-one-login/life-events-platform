package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.di.data.lep.classes.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.GroSex;
import uk.gov.di.data.lep.library.config.Config;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

class GroDeathNotificationMinimisationTest {
    @Mock
    private Context context;

    @Test
    void minimiseGroDeathEventDataReturnsMinimisedData() {
        var config = mockStatic(Config.class);
        config.when(Config::getEnrichmentFields).thenReturn(List.of(
            EnrichmentField.FORENAMES,
            EnrichmentField.SURNAME
        ));
        var underTest = new GroDeathNotificationMinimisation();

        var enrichedData = new GroDeathEventEnrichedData(
            "123a1234-a12b-12a1-a123-123456789012",
            GroSex.FEMALE,
            LocalDate.parse("1972-02-20"),
            LocalDate.parse("2021-12-31"),
            "123456789",
            LocalDate.parse("2022-01-05"),
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

        var result = underTest.handleRequest(enrichedData, context);

        assertEquals("Bob Burt", result.eventDetails.forenames());
        assertEquals("Smith", result.eventDetails.surname());
        assertNull(result.eventDetails.dateOfBirth());
    }
}
