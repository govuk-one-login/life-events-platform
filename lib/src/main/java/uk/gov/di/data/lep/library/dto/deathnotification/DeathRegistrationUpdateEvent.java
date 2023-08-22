package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathRegistrationUpdateEvent(
    DateWithDescription deathDate,
    Integer deathRegistrationID,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason,
    String freeFormatDeathDate,
    StructuredDateTime recordUpdateTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
