package uk.gov.di.data.lep.library.dto.gro;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDate;

public record PersonBirthDateStructure(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    @JsonProperty(value = "PersonBirthDate", namespace = Constants.PERSON_DESCRIPTIVES_NAMESPACE)
    LocalDate personBirthDate,
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty(value = "VerificationLevel", namespace = Constants.PERSON_DESCRIPTIVES_NAMESPACE)
    GroVerificationLevel verificationLevel
) {
}
