package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;
import java.util.Date;

public record PersonDeathDateStructure(
    @JsonProperty("PersonDeathDate")
    LocalDate personDeathDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("VerificationLevel")
    GroVerificationLevel verificationLevel
) {
}
