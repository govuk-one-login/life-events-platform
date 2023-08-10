package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDateTime;

public record GroJsonRecord(
    int RegistrationID,
    int RegistrationType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    LocalDateTime RecordLockedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    LocalDateTime RecordUpdateDateTime,
    int RecordUpdateReason,
    GROPersonNameStructure DeceasedName,
    GROPersonNameStructure DeceasedAliasName,
    String DeceasedAliasNameType,
    String DeceasedMaidenName,
    GenderAtRegistration DeceasedGender,
    PersonDeathDateStructure DeceasedDeathDate,
    int PartialMonthOfDeath,
    int PartialYearOfDeath,
    String QualifierText,
    String FreeFormatDeathDate,
    PersonBirthDateStructure DeceasedBirthDate,
    int PartialMonthOfBirth,
    int PartialYearOfBirth,
    String FreeFormatBirthDate,
    GROAddressStructure DeceasedAddress
) {
}
