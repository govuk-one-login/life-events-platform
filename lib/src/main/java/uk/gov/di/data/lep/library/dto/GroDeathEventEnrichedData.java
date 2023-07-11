package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.enums.GroSex;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GroDeathEventEnrichedData(
    String sourceId,
    GroSex sex,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate dateOfBirth,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    LocalDate dateOfDeath,
    String registrationId,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    LocalDateTime eventTime,
    String verificationLevel,
    String partialMonthOfDeath,
    String partialYearOfDeath,
    String forenames,
    String surname,
    String maidenSurname,
    String addressLine1,
    String addressLine2,
    String addressLine3,
    String addressLine4,
    String postcode) {
}
