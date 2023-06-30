package uk.gov.di.data.lep;

import uk.gov.di.data.lep.enums.EnrichmentField;

import java.util.Arrays;
import java.util.List;

public class Config {

    private static final String envEnrichmentFields = System.getenv("ENRICHMENT_FIELDS") != null
            ? System.getenv("ENRICHMENT_FIELDS")
            : "SOURCE_ID,SURNAME";
    static List<EnrichmentField> enrichmentFields = Arrays.stream(envEnrichmentFields.split("\\s*,\\s*"))
            .map(EnrichmentField::valueOf)
            .toList();


}
