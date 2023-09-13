package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;

@JsonFilter("DeathNotificationSet")
public record DeathRegistrationUpdatedEvent(
    DateWithDescription deathDate,
    Integer deathRegistrationID,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason,
    String freeFormatDeathDate,
    StructuredDateTime recordUpdateTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
