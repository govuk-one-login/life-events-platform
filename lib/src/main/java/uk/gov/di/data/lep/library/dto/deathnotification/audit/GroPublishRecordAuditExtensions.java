package uk.gov.di.data.lep.library.dto.deathnotification.audit;

public record GroPublishRecordAuditExtensions(
    String hashedPayload,
    String correlationID
) {
}
