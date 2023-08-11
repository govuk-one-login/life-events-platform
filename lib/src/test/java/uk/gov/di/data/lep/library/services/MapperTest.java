package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTest {
    @Test
    void ObjectMapperSerialisesStringToDateTimeFormat() throws JsonProcessingException {
        var objectMapperUnderTest = new Mapper().objectMapper();

        var result = objectMapperUnderTest.readValue("{\"dateTime\":\"1958-06-06T12:30:57\"}", MapperTestObject.class);

        assertEquals(LocalDateTime.parse("1958-06-06T12:30:57"), result.dateTime);
    }

    @Test
    void ObjectMapperSerialisesIntToGenderEnum() throws JsonProcessingException {
        var objectMapperUnderTest = new Mapper().objectMapper();

        var maleTest = objectMapperUnderTest.readValue("{\"gender\":1}", MapperTestObject.class);
        var femaleTest = objectMapperUnderTest.readValue("{\"gender\":2}", MapperTestObject.class);
        var indeterminateTest = objectMapperUnderTest.readValue("{\"gender\":9}", MapperTestObject.class);

        assertEquals(GenderAtRegistration.MALE, maleTest.gender);
        assertEquals(GenderAtRegistration.FEMALE, femaleTest.gender);
        assertEquals(GenderAtRegistration.INDETERMINATE, indeterminateTest.gender);
    }

    @Test
    void ObjectMapperSerialisesStringToGroVerificationLevelEnum() throws JsonProcessingException {
        var objectMapperUnderTest = new Mapper().objectMapper();

        var level0Test = objectMapperUnderTest.readValue("{\"verificationLevel\":\"00\"}", MapperTestObject.class);
        var level1Test = objectMapperUnderTest.readValue("{\"verificationLevel\":\"01\"}", MapperTestObject.class);
        var level2Test = objectMapperUnderTest.readValue("{\"verificationLevel\":\"02\"}", MapperTestObject.class);
        var level3Test = objectMapperUnderTest.readValue("{\"verificationLevel\":\"03\"}", MapperTestObject.class);

        assertEquals(GroVerificationLevel.Level_0, level0Test.verificationLevel);
        assertEquals(GroVerificationLevel.Level_1, level1Test.verificationLevel);
        assertEquals(GroVerificationLevel.Level_2, level2Test.verificationLevel);
        assertEquals(GroVerificationLevel.Level_3, level3Test.verificationLevel);
    }

    @Test
    void ObjectMapperReturnsNullForUnknownEnumValuesDuringSerialisation() throws JsonProcessingException {
        var objectMapperUnderTest = new Mapper().objectMapper();

        var genderResult = objectMapperUnderTest.readValue("{\"gender\":\"\"}", MapperTestObject.class);
        var verificationLevelResult = objectMapperUnderTest.readValue("{\"verificationLevel\":\"\"}", MapperTestObject.class);

        assertNull(genderResult.gender);
        assertNull(verificationLevelResult.verificationLevel);
    }

    @Test
    void XmlMapperSerialisesXmlToObject() throws JsonProcessingException {
        var xmlMapperUnderTest = new Mapper().xmlMapper();
        var expected = new MapperTestObject();
        expected.dateTime = (LocalDateTime.parse("2023-06-03T12:34:56"));
        expected.gender = GenderAtRegistration.MALE;
        expected.verificationLevel = GroVerificationLevel.Level_2;

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
}
