package uk.gov.di.data.lep.library.enums;

import java.util.List;

public enum EnrichmentField {
    NAME("name"),
    SEX("sex"),
    DEATH_DATE(List.of("deathDate", "freeFormatDeathDate")),
    BIRTH_DATE("birthDate"),
    ADDRESS("address");
    private final List<String> fieldNames;

    EnrichmentField(final String fieldNames) {
        this.fieldNames = List.of(fieldNames);
    }

    EnrichmentField(final List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }
}
