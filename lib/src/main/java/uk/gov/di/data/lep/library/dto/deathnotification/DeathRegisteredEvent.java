package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;

import java.net.URI;
import java.time.LocalDateTime;

@JsonFilter("DeathNotificationSet")
public record DeathRegisteredEvent(
    DateWithDescription deathDate,
    URI deathRegistration,
    String freeFormatDeathDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    LocalDateTime deathRegistrationTime,
    DeathRegistrationSubject subject
) implements DeathRegistrationBaseEvent {
}
