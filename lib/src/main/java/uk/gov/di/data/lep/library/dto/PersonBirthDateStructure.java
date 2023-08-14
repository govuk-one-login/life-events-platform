package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;

public record PersonBirthDateStructure(
    @JsonProperty("PersonBirthDate")
    LocalDate personBirthDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("VerificationLevel")
    GroVerificationLevel verificationLevel
) {
}
