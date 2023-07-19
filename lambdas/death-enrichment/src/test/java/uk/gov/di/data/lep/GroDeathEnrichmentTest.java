package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.di.data.lep.library.config.Config;
import uk.gov.di.data.lep.library.enums.GroSex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroDeathEnrichmentTest {
    private static final Context context = mock(Context.class);
    private static final LambdaLogger logger = mock(LambdaLogger.class);

    @BeforeAll
    static void setup() {
        when(context.getLogger()).thenReturn(logger);
    }

    @BeforeEach
    void refreshSetup() {
        clearInvocations(logger);
    }

    @Test
    void enrichGroDeathEventDataReturnsEnrichedData() {
        var underTest = new GroDeathEnrichment();

        var sqsMessage = new SQSMessage();
        sqsMessage.setBody("{\"sourceId\":\"123a1234-a12b-12a1-a123-123456789012\"}");
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));

        var result = underTest.handleRequest(sqsEvent, context);

        verify(logger).log("Enriching data (sourceId: 123a1234-a12b-12a1-a123-123456789012)");

        assertEquals("123a1234-a12b-12a1-a123-123456789012", result.sourceId());
        assertEquals(GroSex.FEMALE, result.sex());
        assertEquals(LocalDate.parse("1972-02-20"), result.dateOfBirth());
        assertEquals(LocalDate.parse("2021-12-31"), result.dateOfDeath());
        assertEquals("123456789", result.registrationId());
        assertEquals(LocalDateTime.parse("2022-01-05T12:03:52"), result.eventTime());
        assertEquals("1", result.verificationLevel());
        assertEquals("12", result.partialMonthOfDeath());
        assertEquals("2021", result.partialYearOfDeath());
        assertEquals("Bob Burt", result.forenames());
        assertEquals("Smith", result.surname());
        assertEquals("Jane", result.maidenSurname());
        assertEquals("888 Death House", result.addressLine1());
        assertEquals("8 Death lane", result.addressLine2());
        assertEquals("Deadington", result.addressLine3());
        assertEquals("Deadshire", result.addressLine4());
        assertEquals("XX1 1XX", result.postcode());
    }
}
