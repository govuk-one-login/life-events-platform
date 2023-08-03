package uk.gov.di.data.lep.dto;

public record S3ObjectCreatedNotificationEventObject(
    String key,
    Integer size,
    String etag,
    String sequencer
) {
}
