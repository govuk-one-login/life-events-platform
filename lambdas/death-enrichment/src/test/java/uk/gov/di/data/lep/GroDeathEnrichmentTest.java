package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.di.data.lep.library.dto.GroDeathEventBaseData;
import uk.gov.di.data.lep.library.enums.GroSex;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroDeathEnrichmentTest {
    @Mock
    private Context context;

    @Test
    void enrichGroDeathEventDataReturnsEnrichedData() {
        var underTest = new GroDeathEnrichment();

        var baseData = new GroDeathEventBaseData(
            "123a1234-a12b-12a1-a123-123456789012"
        );

        var result = underTest.handleRequest(baseData, context);

        assertEquals("123a1234-a12b-12a1-a123-123456789012", result.sourceId());
        assertEquals(GroSex.FEMALE, result.sex());
        assertEquals(LocalDate.parse("1972-02-20"), result.dateOfBirth());
        assertEquals(LocalDate.parse("2021-12-31"), result.dateOfDeath());
        assertEquals("123456789", result.registrationId());
        assertEquals(LocalDate.parse("2022-01-05"), result.eventTime());
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
