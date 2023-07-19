package uk.gov.di.data.lep.library.config;

import uk.gov.di.data.lep.library.enums.EnrichmentField;

import java.util.Arrays;
import java.util.List;

public class Config {
    private final String envEnrichmentFields = System.getenv("ENRICHMENT_FIELDS");

    public String getTargetQueue() {
        return System.getenv("TARGET_QUEUE");
    }
    public String getTargetTopic() {
        return System.getenv("TARGET_TOPIC");
    }
    public List<EnrichmentField> getEnrichmentFields() {
        if (envEnrichmentFields == null) {
            return List.of();
        }
        return Arrays.stream(envEnrichmentFields.split("\\s*,\\s*"))
            .map(String::strip)
            .map(EnrichmentField::valueOf)
            .toList();
    }

}
