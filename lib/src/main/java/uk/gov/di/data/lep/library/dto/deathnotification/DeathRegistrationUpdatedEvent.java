package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;

import java.time.LocalDateTime;

@JsonFilter("DeathNotificationSet")
public record DeathRegistrationUpdatedEvent(
    DateWithDescription deathDate,
    Integer deathRegistrationID,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason,
    String freeFormatDeathDate,
    LocalDateTime recordUpdateTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
