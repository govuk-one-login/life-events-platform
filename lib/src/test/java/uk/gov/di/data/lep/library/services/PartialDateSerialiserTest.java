package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.dto.deathnotification.DateWithDescription;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PartialDateSerialiserTest {
    @Test
    void objectMapperMapsCompleteIsoDateToStringCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.writeValueAsString(new DateWithDescription(null, LocalDate.parse("1958-06-06")));

        assertEquals("{\"description\":null,\"value\":\"1958-06-06\"}", result);
    }

    @Test
    void objectMapperMapsYearOnlyIsoDateToStringCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.writeValueAsString(new DateWithDescription("Year only", Year.of(1958)));

        assertEquals("{\"description\":\"Year only\",\"value\":\"1958\"}", result);
    }

    @Test
    void objectMapperMapsMonthYearIsoDateToStringCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.writeValueAsString(new DateWithDescription("Year and month only", YearMonth.of(1958, 6)));

        assertEquals("{\"description\":\"Year and month only\",\"value\":\"1958-06\"}", result);
    }

    @Test
    void objectMapperWritesDirectlyToStringIfNotDefinedType() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.writeValueAsString(new DateWithDescription("Month only", Month.of(6)));

        assertEquals("{\"description\":\"Month only\",\"value\":\"JUNE\"}", result);
    }
}
