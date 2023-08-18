package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.dto.deathnotification.DateWithDescription;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartialDateDeserialiserTest {
    @Test
    void objectMapperMapsCompleteIsoDateCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue("{\"value\":\"1958-06-06\"}", DateWithDescription.class);

        assertEquals(LocalDate.parse("1958-06-06"), result.value());
        assertNull(result.description());
    }

    @Test
    void objectMapperMapsYearOnlyIsoDateCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue(
            "{\"value\":\"1958\",\"description\":\"Year only\"}",
            DateWithDescription.class
        );

        assertEquals(Year.of(1958), result.value());
        assertEquals("Year only", result.description());
    }

    @Test
    void objectMapperMapsMonthYearIsoDateCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue(
            "{\"value\":\"1958-06\",\"description\":\"Year and month only\"}",
            DateWithDescription.class
        );

        assertEquals(YearMonth.of(1958, 6), result.value());
        assertEquals("Year and month only", result.description());
    }

    @Test
    void objectMapperThrowsIfValueIsUnknownString() {
        var objectMapperUnderTest = Mapper.objectMapper();

        var exception = assertThrows(
            JsonMappingException.class,
            () -> objectMapperUnderTest.readValue("{\"value\":\"asdasdasd\",\"description\":\"Year and month only\"}", DateWithDescription.class)
        );

        assertEquals(
            "For input string: \"asdasdasd\" (through reference chain: uk.gov.di.data.lep.library.dto.deathnotification.IsoDate[\"value\"])",
            exception.getLocalizedMessage()
        );
    }

    @Test
    void objectMapperThrowsIfValueHasTooManyHyphens() {
        var objectMapperUnderTest = Mapper.objectMapper();

        var exception = assertThrows(
            JsonMappingException.class,
            () -> objectMapperUnderTest.readValue("{\"value\":\"1958-06-06-06\",\"description\":\"Year and month only\"}", DateWithDescription.class)
        );

        assertEquals(
            "Cannot deserialize value of type `java.time.temporal.TemporalAccessor` from String \"1958-06-06-06\": Not a valid temporal accessor string\n" +
            " at [Source: (String)\"{\"value\":\"1958-06-06-06\",\"description\":\"Year and month only\"}\"; line: 1, column: 10] (through reference chain: uk.gov.di.data.lep.library.dto.deathnotification.IsoDate[\"value\"])",
            exception.getLocalizedMessage()
        );
    }

    @Test
    void objectMapperThrowsIfValueIsNotString() {
        var objectMapperUnderTest = Mapper.objectMapper();

        var exception = assertThrows(
            JsonMappingException.class,
            () -> objectMapperUnderTest.readValue("{\"value\":12345,\"description\":\"Year and month only\"}", DateWithDescription.class)
        );

        assertEquals(
            "Unexpected token (VALUE_NUMBER_INT), expected VALUE_STRING: Not a valid temporal accessor string\n" +
            " at [Source: (String)\"{\"value\":12345,\"description\":\"Year and month only\"}\"; line: 1, column: 10] (through reference chain: uk.gov.di.data.lep.library.dto.deathnotification.IsoDate[\"value\"])",
            exception.getLocalizedMessage()
        );
    }
}
