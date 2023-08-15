package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GroDeathEventDetails(
    GenderAtRegistration gender,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    LocalDate dateOfBirth,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    LocalDate dateOfDeath,
    Integer registrationId,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    LocalDateTime eventTime,
    Integer partialMonthOfDeath,
    Integer partialYearOfDeath,
    List<String> forenames,
    String surname,
    String maidenSurname,
    String flat,
    String building,
    List<String> lines,
    String postcode) {
}
