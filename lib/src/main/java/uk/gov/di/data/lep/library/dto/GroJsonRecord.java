package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDateTime;

public record GroJsonRecord(
    int RegistrationID,
    @JsonIgnoreProperties(ignoreUnknown = true)
    int RegistrationType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonIgnoreProperties(ignoreUnknown = true)
    LocalDateTime RecordLockedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonIgnoreProperties(ignoreUnknown = true)
    LocalDateTime RecordUpdateDateTime,
    @JsonIgnoreProperties(ignoreUnknown = true)
    int RecordUpdateReason,
    GROPersonNameStructure DeceasedName,
    @JsonIgnoreProperties(ignoreUnknown = true)
    GROPersonNameStructure DeceasedAliasName,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String DeceasedAliasNameType,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String DeceasedMaidenName,
    GenderAtRegistration DeceasedGender,
    @JsonIgnoreProperties(ignoreUnknown = true)
    PersonDeathDateStructure DeceasedDeathDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    int PartialMonthOfDeath,
    @JsonIgnoreProperties(ignoreUnknown = true)
    int PartialYearOfDeath,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String QualifierText,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String FreeFormatDeathDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    PersonBirthDateStructure DeceasedBirthDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    int PartialMonthOfBirth,
    @JsonIgnoreProperties(ignoreUnknown = true)
    int PartialYearOfBirth,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String FreeFormatBirthDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    GROAddressStructure DeceasedAddress
) {
}
