package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.ZonedDateTime;

@JsonFilter("DeathNotificationSet")
public record DeathRegisteredEvent(
    DateWithDescription deathDate,
    URI deathRegistration,
    String freeFormatDeathDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.ZONED_DATE_TIME_PATTERN)
    ZonedDateTime deathRegistrationTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
