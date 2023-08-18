package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathRegistrationEvent(
    IsoDate deathDate,
    Integer deathRegistrationID,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason,
    String freeFormatDeathDate,
    StructuredDateTime recordUpdateTime,
    StructuredDateTime deathRegistrationTime,
    DeathRegistrationSubject subject
) {
}
