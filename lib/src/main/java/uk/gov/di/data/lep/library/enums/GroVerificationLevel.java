package uk.gov.di.data.lep.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GroVerificationLevel {
    LEVEL_0("00"),
    LEVEL_1("01"),
    LEVEL_2("02"),
    LEVEL_3("03");
    private final String value;

    GroVerificationLevel(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
