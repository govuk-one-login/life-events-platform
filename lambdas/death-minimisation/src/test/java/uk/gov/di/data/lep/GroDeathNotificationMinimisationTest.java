package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.GroSex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroDeathNotificationMinimisationTest {
    @Mock
    private static Context context = mock(Context.class);
    @Mock
    private static LambdaLogger logger = mock(LambdaLogger.class);

    @BeforeAll
    static void setup() {
        when(context.getLogger()).thenReturn(logger);
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(logger);
    }

    @Test
    void minimiseGroDeathEventDataReturnsMinimisedDataWithNoEnrichmentFields() {
        var config = mockStatic(Config.class);
        config.when(Config::getEnrichmentFields).thenReturn(List.of());
        var underTest = new GroDeathNotificationMinimisation();

        var enrichedData = new GroDeathEventEnrichedData(
            "123a1234-a12b-12a1-a123-123456789012",
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

        var result = underTest.handleRequest(enrichedData, context);

        config.close();

        verify(logger).log("Minimising enriched data (sourceId: 123a1234-a12b-12a1-a123-123456789012)");

        assertNull(result.eventDetails().sex());
        assertNull(result.eventDetails().dateOfBirth());
        assertNull(result.eventDetails().dateOfDeath());
        assertNull(result.eventDetails().registrationId());
        assertNull(result.eventDetails().eventTime());
        assertNull(result.eventDetails().verificationLevel());
        assertNull(result.eventDetails().partialMonthOfDeath());
        assertNull(result.eventDetails().partialYearOfDeath());
        assertNull(result.eventDetails().forenames());
        assertNull(result.eventDetails().surname());
        assertNull(result.eventDetails().maidenSurname());
        assertNull(result.eventDetails().addressLine1());
        assertNull(result.eventDetails().addressLine2());
        assertNull(result.eventDetails().addressLine3());
        assertNull(result.eventDetails().addressLine4());
    }

    @Test
    void minimiseGroDeathEventDataReturnsMinimisedDataWithPartialEnrichmentFields() {
        var config = mockStatic(Config.class);
        config.when(Config::getEnrichmentFields).thenReturn(List.of(
            EnrichmentField.SEX,
            EnrichmentField.DATE_OF_DEATH,
            EnrichmentField.FORENAMES,
            EnrichmentField.SURNAME,
            EnrichmentField.POSTCODE
        ));
        var underTest = new GroDeathNotificationMinimisation();

        var enrichedData = new GroDeathEventEnrichedData(
            "123a1234-a12b-12a1-a123-123456789012",
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

        var result = underTest.handleRequest(enrichedData, context);

        config.close();

        verify(logger).log("Minimising enriched data (sourceId: 123a1234-a12b-12a1-a123-123456789012)");

        assertEquals(GroSex.FEMALE, result.eventDetails().sex());
        assertNull(result.eventDetails().dateOfBirth());
        assertEquals(LocalDate.parse("2021-12-31"), result.eventDetails().dateOfDeath());
        assertNull(result.eventDetails().registrationId());
        assertNull(result.eventDetails().eventTime());
        assertNull(result.eventDetails().verificationLevel());
        assertNull(result.eventDetails().partialMonthOfDeath());
        assertNull(result.eventDetails().partialYearOfDeath());
        assertEquals("Bob Burt", result.eventDetails().forenames());
        assertEquals("Smith", result.eventDetails().surname());
        assertNull(result.eventDetails().maidenSurname());
        assertNull(result.eventDetails().addressLine1());
        assertNull(result.eventDetails().addressLine2());
        assertNull(result.eventDetails().addressLine3());
        assertNull(result.eventDetails().addressLine4());
    }

    @Test
    void minimiseGroDeathEventDataReturnsMinimisedDataWithAllEnrichmentFields() {
        var config = mockStatic(Config.class);
        config.when(Config::getEnrichmentFields).thenReturn(List.of(
            EnrichmentField.SEX,
            EnrichmentField.DATE_OF_BIRTH,
            EnrichmentField.DATE_OF_DEATH,
            EnrichmentField.REGISTRATION_ID,
            EnrichmentField.EVENT_TIME,
            EnrichmentField.VERIFICATION_LEVEL,
            EnrichmentField.PARTIAL_MONTH_OF_DEATH,
            EnrichmentField.PARTIAL_YEAR_OF_DEATH,
            EnrichmentField.FORENAMES,
            EnrichmentField.SURNAME,
            EnrichmentField.MAIDEN_SURNAME,
            EnrichmentField.ADDRESS_LINE_1,
            EnrichmentField.ADDRESS_LINE_2,
            EnrichmentField.ADDRESS_LINE_3,
            EnrichmentField.ADDRESS_LINE_4,
            EnrichmentField.POSTCODE
        ));
        var underTest = new GroDeathNotificationMinimisation();

        var enrichedData = new GroDeathEventEnrichedData(
            "123a1234-a12b-12a1-a123-123456789012",
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

        var result = underTest.handleRequest(enrichedData, context);

        config.close();

        verify(logger).log("Minimising enriched data (sourceId: 123a1234-a12b-12a1-a123-123456789012)");

        assertEquals(GroSex.FEMALE, result.eventDetails().sex());
        assertEquals(LocalDate.parse("1972-02-20"), result.eventDetails().dateOfBirth());
        assertEquals(LocalDate.parse("2021-12-31"), result.eventDetails().dateOfDeath());
        assertEquals("123456789", result.eventDetails().registrationId());
        assertEquals(LocalDateTime.parse("2022-01-05T12:03:52"), result.eventDetails().eventTime());
        assertEquals("1", result.eventDetails().verificationLevel());
        assertEquals("12", result.eventDetails().partialMonthOfDeath());
        assertEquals("2021", result.eventDetails().partialYearOfDeath());
        assertEquals("Bob Burt", result.eventDetails().forenames());
        assertEquals("Smith", result.eventDetails().surname());
        assertEquals("Jane", result.eventDetails().maidenSurname());
        assertEquals("888 Death House", result.eventDetails().addressLine1());
        assertEquals("8 Death lane", result.eventDetails().addressLine2());
        assertEquals("Deadington", result.eventDetails().addressLine3());
        assertEquals("Deadshire", result.eventDetails().addressLine4());
        assertEquals("XX1 1XX", result.eventDetails().postcode());
    }
}
