package uk.gov.di.data.lep.library.services;

import uk.gov.di.data.lep.library.enums.GenderAtRegistration;
import uk.gov.di.data.lep.library.enums.GroVerificationLevel;

import java.time.LocalDateTime;

public record MapperTestObject (
    LocalDateTime dateTime,
    GenderAtRegistration gender,
    GroVerificationLevel verificationLevel
){
}
