package uk.gov.di.data.lep.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GroVerificationLevel {
    Level_0("00"),
    Level_1("01"),
    Level_2("02"),
    Level_3("03");
    private final String value;

    GroVerificationLevel(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
