package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.di.data.lep.library.dto.deathnotification.IsoDate;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTest {
    @Test
    void objectMapperSerialisesStringToDateTimeFormat() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue("{\"dateTime\":\"1958-06-06T12:30:57\"}", MapperTestObject.class);

        assertEquals(LocalDateTime.parse("1958-06-06T12:30:57"), result.dateTime);
    }

    @ParameterizedTest
    @CsvSource(value = {"1:MALE", "2:FEMALE", "9:INDETERMINATE"}, delimiter = ':')
    void objectMapperSerialisesIntToGenderEnum(String input, GenderAtRegistration expected) throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue(String.format("{\"gender\":%s}", input), MapperTestObject.class);

        assertEquals(expected, result.gender);
    }

    @ParameterizedTest
    @CsvSource(value = {"00:LEVEL_0", "01:LEVEL_1", "02:LEVEL_2", "03:LEVEL_3"}, delimiter = ':')
    void objectMapperSerialisesStringToGroVerificationLevelEnum(String input, GroVerificationLevel expected) throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue(String.format("{\"verificationLevel\":\"%s\"}", input), MapperTestObject.class);

        assertEquals(expected, result.verificationLevel);
    }

    @Test
    void objectMapperReturnsNullForUnknownEnumValuesDuringSerialisation() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var genderResult = objectMapperUnderTest.readValue("{\"gender\":\"\"}", MapperTestObject.class);
        var verificationLevelResult = objectMapperUnderTest.readValue("{\"verificationLevel\":\"\"}", MapperTestObject.class);

        assertNull(genderResult.gender);
        assertNull(verificationLevelResult.verificationLevel);
    }

    @Test
    void xmlMapperSerialisesXmlToObject() throws JsonProcessingException {
        var xmlMapperUnderTest = Mapper.xmlMapper();
        var expected = new MapperTestObject();
        expected.dateTime = (LocalDateTime.parse("2023-06-03T12:34:56"));
        expected.gender = GenderAtRegistration.MALE;
        expected.verificationLevel = GroVerificationLevel.LEVEL_2;

        var result = xmlMapperUnderTest.readValue(
            "<MapperTestObject>" +
                "<dateTime>2023-06-03T12:34:56</dateTime>" +
                "<gender>1</gender>" +
                "<verificationLevel>02</verificationLevel>" +
                "</MapperTestObject>",
            MapperTestObject.class
        );

        assertEquals(expected.dateTime, result.dateTime);
        assertEquals(expected.gender, result.gender);
        assertEquals(expected.verificationLevel, result.verificationLevel);
    }

    @Test
    void objectMapperMapsCompleteIsoDateCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue("{\"isoDate\":{\"value\":\"1958-06-06\"}}", MapperTestObject.class);

        assertEquals(LocalDate.parse("1958-06-06"), result.isoDate.value());
        assertNull(result.isoDate.description());
    }

    @Test
    void objectMapperMapsYearOnlyIsoDateCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue("{\"isoDate\":{\"value\":\"1958\",\"description\":\"Year only\"}}", MapperTestObject.class);

        assertEquals(Year.of(1958), result.isoDate.value());
        assertEquals("Year only", result.isoDate.description());
    }

    @Test
    void objectMapperMapsYearOnlyIsoDateToStringCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.writeValueAsString(new IsoDate("Year only", Year.of(1958)));

        assertEquals("{\"description\":\"Year only\",\"value\":\"1958\"}", result);
    }

    @Test
    void objectMapperMapsMonthYearIsoDateCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.readValue("{\"isoDate\":{\"value\":\"1958-06\",\"description\":\"Year and month only\"}}", MapperTestObject.class);

        assertEquals(YearMonth.of(1958, 6), result.isoDate.value());
        assertEquals("Year and month only", result.isoDate.description());
    }

    @Test
    void objectMapperMapsMonthYearIsoDateToStringCorrectly() throws JsonProcessingException {
        var objectMapperUnderTest = Mapper.objectMapper();

        var result = objectMapperUnderTest.writeValueAsString(new IsoDate("Year and month only", YearMonth.of(1958, 6)));

        assertEquals("{\"description\":\"Year and month only\",\"value\":\"1958-06\"}", result);
    }
}
