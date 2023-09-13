package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;

import java.net.URI;
import java.time.OffsetDateTime;

@JsonFilter("DeathNotificationSet")
public record DeathRegistrationUpdatedEvent(
    DateWithDescription deathDate,
    URI deathRegistration,
    DeathRegistrationUpdateReasonType deathRegistrationUpdateReason,
    String freeFormatDeathDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.ZONED_DATE_TIME_PATTERN)
    OffsetDateTime recordUpdateTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
