package uk.gov.di.data.lep.library.dto.DeathNotification;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.di.data.lep.library.enums.GenderAtRegistration;

import java.util.Arrays;
import java.util.Objects;

public enum Sex {
    MALE("Male", GenderAtRegistration.MALE),
    FEMALE("Female", GenderAtRegistration.FEMALE),
    INDETERMINATE("Indeterminate", GenderAtRegistration.INDETERMINATE);
    private final String value;
    private final GenderAtRegistration genderAtRegistration;

    Sex(String value, GenderAtRegistration genderAtRegistration) {
        this.value = value;
        this.genderAtRegistration = genderAtRegistration;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Sex fromGro(GenderAtRegistration genderAtRegistration) {
        return Arrays.stream(Sex.values())
            .filter(s -> Objects.equals(s.genderAtRegistration, genderAtRegistration))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(genderAtRegistration.toString()));
    }
}
