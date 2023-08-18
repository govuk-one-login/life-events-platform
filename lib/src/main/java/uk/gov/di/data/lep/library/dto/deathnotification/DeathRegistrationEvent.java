package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathRegistrationEvent(
    DateWithDescription deathDate,
    Integer deathRegistrationID,
    String freeFormatDeathDate,
    StructuredDateTime deathRegistrationTime,
    DeathRegistrationSubject subject
) {
}
