package uk.gov.di.data.lep.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GenderAtRegistration {
    MALE(1),
    FEMALE(2),
    INDETERMINATE(9);
    private final int value;

    GenderAtRegistration(final int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
