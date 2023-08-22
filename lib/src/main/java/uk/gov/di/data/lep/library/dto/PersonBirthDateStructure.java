package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;

public record PersonBirthDateStructure(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    @JsonAlias("PersonBirthDate")
    LocalDate personBirthDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAlias("VerificationLevel")
    GroVerificationLevel verificationLevel
) {
}
