package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathRegistrationEvent(
    IsoDate deathDate,
    Integer deathRegistrationID,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason, // Only present on update
    String freeFormatDeathDate,
    StructuredDateTime recordUpdateTime, // Only present on update
    StructuredDateTime deathRegistrationTime, // Only present on even
    DeathRegistrationSubject subject
) {
}
