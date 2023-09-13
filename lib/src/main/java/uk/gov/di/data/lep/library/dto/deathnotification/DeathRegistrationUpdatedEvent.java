package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;

import java.net.URI;
import java.time.LocalDateTime;

@JsonFilter("DeathNotificationSet")
public record DeathRegistrationUpdatedEvent(
    DateWithDescription deathDate,
    URI deathRegistration,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason,
    String freeFormatDeathDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    LocalDateTime recordUpdateTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
