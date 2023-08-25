package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonFormat;
import uk.gov.di.data.lep.library.config.Constants;

import java.time.LocalDateTime;

public record StructuredDateTime(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.LOCAL_DATE_TIME_PATTERN)
    LocalDateTime value
) {
}
