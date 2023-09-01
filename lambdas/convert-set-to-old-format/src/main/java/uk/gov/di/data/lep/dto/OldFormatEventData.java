package uk.gov.di.data.lep.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;

import java.time.LocalDate;

public record OldFormatEventData (
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    LocalDate registrationDate,
    String firstNames,
    String lastName,
    String sex,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    LocalDate dateOfDeath,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_PATTERN)
    LocalDate dateOfBirth,
    String birthPlace,
    String deathPlace,
    String maidenName,
    String occupation,
    String retired,
    String address
){
}
