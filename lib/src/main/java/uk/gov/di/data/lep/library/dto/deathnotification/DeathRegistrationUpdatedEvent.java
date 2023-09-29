package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;

import java.net.URI;
import java.time.ZonedDateTime;

@JsonFilter("DeathNotificationSet")
public record DeathRegistrationUpdatedEvent(
    DateWithDescription deathDate,
    URI deathRegistration,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason,
    String freeFormatDeathDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.ZONED_DATE_TIME_PATTERN)
    ZonedDateTime recordUpdateTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
