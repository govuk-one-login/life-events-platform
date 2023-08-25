package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.di.data.lep.library.enums.EnrichmentField;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTest {
    private static final ObjectMapper objectMapperUnderTest = Mapper.objectMapper();
    private static final XmlMapper xmlMapperUnderTest = Mapper.xmlMapper();

    @Test
    void objectMapperSerialisesStringToDateTimeFormat() throws JsonProcessingException {
        var result = objectMapperUnderTest.readValue("{\"dateTime\":\"1958-06-06T12:30:57\"}", MapperTestObject.class);

        assertEquals(LocalDateTime.parse("1958-06-06T12:30:57"), result.dateTime());
    }

    @ParameterizedTest
    @CsvSource(value = {"1:MALE", "2:FEMALE", "9:INDETERMINATE"}, delimiter = ':')
    void objectMapperSerialisesIntToGenderEnum(String input, GenderAtRegistration expected) throws JsonProcessingException {
        var result = objectMapperUnderTest.readValue(String.format("{\"gender\":%s}", input), MapperTestObject.class);

        assertEquals(expected, result.gender());
    }

    @ParameterizedTest
    @CsvSource(value = {"00:LEVEL_0", "01:LEVEL_1", "02:LEVEL_2", "03:LEVEL_3"}, delimiter = ':')
    void objectMapperSerialisesStringToGroVerificationLevelEnum(String input, GroVerificationLevel expected) throws JsonProcessingException {
        var result = objectMapperUnderTest.readValue(String.format("{\"verificationLevel\":\"%s\"}", input), MapperTestObject.class);

        assertEquals(expected, result.verificationLevel());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "NAME:NAME",
            "SEX:SEX",
            "DEATH_DATE:DEATH_DATE",
            "BIRTH_DATE:BIRTH_DATE",
            "ADDRESS:ADDRESS"
        },
        delimiter = ':'
    )
    void objectMapperSerialisesStringToEnrichmentFieldEnum(String input, EnrichmentField expected) throws JsonProcessingException {
        var result = objectMapperUnderTest.readValue(String.format("{\"enrichmentField\":\"%s\"}", input), MapperTestObject.class);

        assertEquals(expected, result.enrichmentField());
    }

    @Test
    void objectMapperReturnsNullForUnknownEnumValuesDuringSerialisation() throws JsonProcessingException {
        var genderResult = objectMapperUnderTest.readValue("{\"gender\":\"\"}", MapperTestObject.class);
        var verificationLevelResult = objectMapperUnderTest.readValue("{\"verificationLevel\":\"\"}", MapperTestObject.class);

        assertNull(genderResult.gender());
        assertNull(verificationLevelResult.verificationLevel());
    }

    @Test
    void xmlMapperSerialisesXmlToObject() throws JsonProcessingException {
        var expected = new MapperTestObject(
            LocalDateTime.parse("2023-06-03T12:34:56"),
            EnrichmentField.NAME,
            GenderAtRegistration.MALE,
            GroVerificationLevel.LEVEL_2
        );

        var result = xmlMapperUnderTest.readValue(
            "<MapperTestObject>" +
                "<dateTime>2023-06-03T12:34:56</dateTime>" +
                "<gender>1</gender>" +
                "<verificationLevel>02</verificationLevel>" +
                "</MapperTestObject>",
            MapperTestObject.class
        );

        assertEquals(expected.dateTime(), result.dateTime());
        assertEquals(expected.gender(), result.gender());
        assertEquals(expected.verificationLevel(), result.verificationLevel());
    }
}
