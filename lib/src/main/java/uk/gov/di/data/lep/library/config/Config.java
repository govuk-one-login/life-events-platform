package uk.gov.di.data.lep.library.config;

import uk.gov.di.data.lep.library.enums.EnrichmentField;

import java.util.Arrays;
import java.util.List;

public class Config {
    private final String envEnrichmentFields = System.getenv("ENRICHMENT_FIELDS");

    public List<EnrichmentField> getEnrichmentFields() {
        if (envEnrichmentFields == null) {
            return List.of();
        }
        return Arrays.stream(envEnrichmentFields.split("\\s*,\\s*"))
            .map(String::strip)
            .map(EnrichmentField::valueOf)
            .toList();
    }

    public String getDomainName() {
        return System.getenv("DOMAIN_NAME");
    }

    public String getAwsRegion() {
        return System.getenv("AWS_REGION");
    }

    public String getCognitoClientId() {
        return System.getenv("COGNITO_CLIENT_ID");
    }

    public String getCognitoDomainName() {
        return System.getenv("COGNITO_DOMAIN_NAME");
    }

    public String getGroRecordsBucketName() {
        return System.getenv("GRO_RECORDS_BUCKET_NAME");
    }

    public String getTargetQueue() {
        return System.getenv("TARGET_QUEUE");
    }

    public String getTargetTopic() {
        return System.getenv("TARGET_TOPIC");
    }

    public String getUserPoolId() {
        return System.getenv("USER_POOL_ID");
    }
}
