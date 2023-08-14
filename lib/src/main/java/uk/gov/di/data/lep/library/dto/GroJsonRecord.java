package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDateTime;

public record GroJsonRecord(
    @JsonProperty("RegistrationID")
    int registrationId,
    @JsonProperty("RegistrationType")
    int registrationType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonProperty("RecordLockedDateTime")
    LocalDateTime recordLockedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonProperty("RecordUpdateDateTime")
    LocalDateTime recordUpdateDateTime,
    @JsonProperty("RecordUpdateReason")
    int recordUpdateReason,
    @JsonProperty("DeceasedName")
    GroPersonNameStructure deceasedName,
    @JsonProperty("DeceasedAliasName")
    GroPersonNameStructure deceasedAliasName,
    @JsonProperty("DeceasedAliasNameType")
    String deceasedAliasNameType,
    @JsonProperty("DeceasedMaidenName")
    String deceasedMaidenName,
    @JsonProperty("DeceasedGender")
    GenderAtRegistration deceasedGender,
    @JsonProperty("DeceasedDeathDate")
    PersonDeathDateStructure deceasedDeathDate,
    @JsonProperty("PartialMonthOfDeath")
    int partialMonthOfDeath,
    @JsonProperty("PartialYearOfDeath")
    int partialYearOfDeath,
    @JsonProperty("QualifierText")
    String qualifierText,
    @JsonProperty("FreeFormatDeathDate")
    String freeFormatDeathDate,
    @JsonProperty("DeceasedBirthDate")
    PersonBirthDateStructure deceasedBirthDate,
    @JsonProperty("PartialMonthOfBirth")
    int partialMonthOfBirth,
    @JsonProperty("PartialYearOfBirth")
    int partialYearOfBirth,
    @JsonProperty("FreeFormatBirthDate")
    String freeFormatBirthDate,
    @JsonProperty("DeceasedAddress")
    GroAddressStructure deceasedAddress
) {
}
