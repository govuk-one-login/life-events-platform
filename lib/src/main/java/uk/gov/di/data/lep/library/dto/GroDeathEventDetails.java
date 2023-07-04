package uk.gov.di.data.lep.library.dto;

import uk.gov.di.data.lep.library.enums.GroSex;

import java.time.LocalDate;

public record GroDeathEventDetails(
    GroSex sex,
    LocalDate dateOfBirth,
    LocalDate dateOfDeath,
    String registrationId,
    LocalDate eventTime,
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
