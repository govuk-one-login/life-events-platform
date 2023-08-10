package uk.gov.di.data.lep.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperTest {
    private static final ObjectMapper underTest = new Mapper().objectMapper();

    @Test
    void MapperHandlesDateTimeFormat() throws JsonProcessingException {
        var result = underTest.readValue("{\"dateTime\":\"1958-06-06T12:30:57\"}", MapperTestObject.class);

        assertInstanceOf(LocalDateTime.class, result.dateTime);
    }

    @Test
    void MapperSerialisesIntToGenderEnum() throws JsonProcessingException {
        var maleTest = underTest.readValue("{\"gender\":1}", MapperTestObject.class);
        var femaleTest = underTest.readValue("{\"gender\":2}", MapperTestObject.class);
        var indeterminateTest = underTest.readValue("{\"gender\":9}", MapperTestObject.class);

        assertEquals(GenderAtRegistration.MALE, maleTest.gender);
        assertEquals(GenderAtRegistration.FEMALE, femaleTest.gender);
        assertEquals(GenderAtRegistration.INDETERMINATE, indeterminateTest.gender);
    }

    @Test
    void MapperSerialisesStringToGroVerificationLevelEnum() throws JsonProcessingException {
        var level0Test = underTest.readValue("{\"verificationLevel\":\"00\"}", MapperTestObject.class);
        var level1Test = underTest.readValue("{\"verificationLevel\":\"01\"}", MapperTestObject.class);
        var level2Test = underTest.readValue("{\"verificationLevel\":\"02\"}", MapperTestObject.class);
        var level3Test = underTest.readValue("{\"verificationLevel\":\"03\"}", MapperTestObject.class);

        assertEquals(GroVerificationLevel.Level_0, level0Test.verificationLevel);
        assertEquals(GroVerificationLevel.Level_1, level1Test.verificationLevel);
        assertEquals(GroVerificationLevel.Level_2, level2Test.verificationLevel);
        assertEquals(GroVerificationLevel.Level_3, level3Test.verificationLevel);
    }

    @Test
    void MapperReturnsNullForUnknownEnumValuesDuringSerialisation() throws JsonProcessingException {
        var genderResult = underTest.readValue("{\"gender\":\"\"}", MapperTestObject.class);
        var verificationLevelResult = underTest.readValue("{\"verificationLevel\":\"\"}", MapperTestObject.class);

        assertNull(genderResult.gender);
        assertNull(verificationLevelResult.verificationLevel);
    }
}
