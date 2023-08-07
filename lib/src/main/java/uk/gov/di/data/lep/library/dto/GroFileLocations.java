package uk.gov.di.data.lep.library.dto;

public record GroFileLocations(
    String xmlBucket,
    String xmlKey,
    String jsonBucket,
    String jsonKey
) {}
