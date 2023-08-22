package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDateTime;
import java.util.List;

public record GroJsonRecord(
    @JsonAlias("RegistrationID")
    Integer registrationID,
    @JsonAlias("RegistrationType")
    Integer registrationType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonAlias("RecordLockedDateTime")
    LocalDateTime recordLockedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonAlias("RecordUpdateDateTime")
    LocalDateTime recordUpdateDateTime,
    @JsonAlias("RecordUpdateReason")
    Integer recordUpdateReason,
    @JsonAlias("DeceasedName")
    GroPersonNameStructure deceasedName,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonAlias("DeceasedAliasName")
    List<GroPersonNameStructure> deceasedAliasNames,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonAlias("DeceasedAliasNameType")
    List<String> deceasedAliasNameTypes,
    @JsonAlias("DeceasedMaidenName")
    String deceasedMaidenName,
    @JsonAlias("DeceasedGender")
    GenderAtRegistration deceasedGender,
    @JsonAlias("DeceasedDeathDate")
    PersonDeathDateStructure deceasedDeathDate,
    @JsonAlias("PartialMonthOfDeath")
    Integer partialMonthOfDeath,
    @JsonAlias("PartialYearOfDeath")
    Integer partialYearOfDeath,
    @JsonAlias("QualifierText")
    String qualifierText,
    @JsonAlias("FreeFormatDeathDate")
    String freeFormatDeathDate,
    @JsonAlias("DeceasedBirthDate")
    PersonBirthDateStructure deceasedBirthDate,
    @JsonAlias("PartialMonthOfBirth")
    Integer partialMonthOfBirth,
    @JsonAlias("PartialYearOfBirth")
    Integer partialYearOfBirth,
    @JsonAlias("FreeFormatBirthDate")
    String freeFormatBirthDate,
    @JsonAlias("DeceasedAddress")
    GroAddressStructure deceasedAddress
) {
}
