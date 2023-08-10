package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;

public record PersonBirthDateStructure(
    LocalDate PersonBirthDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    GroVerificationLevel VerificationLevel
) {
}
