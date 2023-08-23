package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
public record GroJsonRecord(
    @JsonProperty("RegistrationID")
    Integer registrationID,
    @JsonProperty("RegistrationType")
    Integer registrationType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonProperty("RecordLockedDateTime")
    LocalDateTime recordLockedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    @JsonProperty("RecordUpdateDateTime")
    LocalDateTime recordUpdateDateTime,
    @JsonProperty("RecordUpdateReason")
    Integer recordUpdateReason,
    @JsonProperty("DeceasedName")
    GroPersonNameStructure deceasedName,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("DeceasedAliasName")
    List<GroPersonNameStructure> deceasedAliasNames,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("DeceasedAliasNameType")
    List<String> deceasedAliasNameTypes,
    @JsonProperty("DeceasedMaidenName")
    String deceasedMaidenName,
    @JsonProperty("DeceasedGender")
    GenderAtRegistration deceasedGender,
    @JsonProperty("DeceasedDeathDate")
    PersonDeathDateStructure deceasedDeathDate,
    @JsonProperty("PartialMonthOfDeath")
    Integer partialMonthOfDeath,
    @JsonProperty("PartialYearOfDeath")
    Integer partialYearOfDeath,
    @JsonProperty("QualifierText")
    String qualifierText,
    @JsonProperty("FreeFormatDeathDate")
    String freeFormatDeathDate,
    @JsonProperty("DeceasedBirthDate")
    PersonBirthDateStructure deceasedBirthDate,
    @JsonProperty("PartialMonthOfBirth")
    Integer partialMonthOfBirth,
    @JsonProperty("PartialYearOfBirth")
    Integer partialYearOfBirth,
    @JsonProperty("FreeFormatBirthDate")
    String freeFormatBirthDate,
    @JsonProperty("DeceasedAddress")
    GroAddressStructure deceasedAddress
) {
}
