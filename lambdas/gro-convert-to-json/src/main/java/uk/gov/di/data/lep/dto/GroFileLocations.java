package uk.gov.di.data.lep.dto;

public record GroFileLocations(
    String xmlBucket,
    String xmlKey,
    String jsonBucket,
    String jsonKey
) {}
