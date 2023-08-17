package uk.gov.di.data.lep.library.dto.DeathNotification;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.util.Arrays;
import java.util.Objects;

public enum DeathRegistrationUpdateReasonType {
    FORMAL_CORRECTION("formal_correction", 1),
    QUALITY_ASSURANCE("quality_assurance", 2),
    TYPOGRAPHICAL("typographical", 3),
    CANCELLED("cancelled", 4),
    CANCELLATION_REMOVED("cancellation_removed", 5);
    private final String value;
    private final Integer number;

    DeathRegistrationUpdateReasonType(String value, Integer number) {
        this.value = value;
        this.number = number;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static DeathRegistrationUpdateReasonType fromGroRegistrationType(Integer groRegistrationType) {
        return Arrays.stream(DeathRegistrationUpdateReasonType.values())
            .filter(d -> Objects.equals(d.number, groRegistrationType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(groRegistrationType.toString()));
    }
}
