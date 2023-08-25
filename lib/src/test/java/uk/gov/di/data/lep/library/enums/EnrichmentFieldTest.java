package uk.gov.di.data.lep.library.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnrichmentFieldTest {
    @ParameterizedTest
    @CsvSource(value = {"name:NAME", "sex:SEX", "birthDate:BIRTH_DATE", "address:ADDRESS"}, delimiter = ':')
    void enrichmentFieldMapsToCorrectSingleField(String input, EnrichmentField expected) {
        assertEquals(List.of(input), expected.getFieldNames());
    }

    @Test
    void enrichmentFieldMapsToCorrectMultipleFields() {
        assertEquals(List.of("deathDate", "freeFormatDeathDate"), EnrichmentField.DEATH_DATE.getFieldNames());
    }
}
