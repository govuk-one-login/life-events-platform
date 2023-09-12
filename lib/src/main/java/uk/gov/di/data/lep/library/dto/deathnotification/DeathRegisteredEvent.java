package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;

import java.time.LocalDateTime;

@JsonFilter("DeathNotificationSet")
public record DeathRegisteredEvent(
    DateWithDescription deathDate,
    Integer deathRegistrationID,
    String freeFormatDeathDate,
    LocalDateTime deathRegistrationTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
