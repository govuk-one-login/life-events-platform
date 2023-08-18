package uk.gov.di.data.lep.library.dto.deathnotification;


import com.fasterxml.jackson.annotation.JsonValue;

public enum NamePartType {
    GIVEN_NAME("GivenName"),
    FAMILY_NAME("FamilyName");
    private final String value;

    NamePartType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
