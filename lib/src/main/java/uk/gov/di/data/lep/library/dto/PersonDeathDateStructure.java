package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;

public record PersonDeathDateStructure(
    @JsonAlias("PersonDeathDate")
    LocalDate personDeathDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAlias("VerificationLevel")
    GroVerificationLevel verificationLevel
) {
}
