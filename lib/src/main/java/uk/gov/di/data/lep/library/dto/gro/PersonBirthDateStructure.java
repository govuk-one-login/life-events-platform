package uk.gov.di.data.lep.library.dto.gro;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;

public record PersonBirthDateStructure(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    @JsonProperty("PersonBirthDate")
    LocalDate personBirthDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("VerificationLevel")
    GroVerificationLevel verificationLevel
) {
}