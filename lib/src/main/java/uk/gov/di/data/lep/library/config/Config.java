package uk.gov.di.data.lep.library.config;

import uk.gov.di.data.lep.library.enums.EnrichmentField;

import java.util.Arrays;
import java.util.List;

public class Config {
    private static final String envEnrichmentFields = System.getenv("ENRICHMENT_FIELDS");

    public static List<EnrichmentField> getEnrichmentFields() {
        if (envEnrichmentFields == null) {
            return List.of();
        }
        return Arrays.stream(envEnrichmentFields.split("\\s*,\\s*"))
            .map(EnrichmentField::valueOf)
            .toList();
    }
}
