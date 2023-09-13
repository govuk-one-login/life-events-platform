package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;

@JsonFilter("DeathNotificationSet")
public record DeathRegisteredEvent(
    DateWithDescription deathDate,
    Integer deathRegistrationID,
    String freeFormatDeathDate,
    StructuredDateTime deathRegistrationTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
