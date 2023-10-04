package uk.gov.di.data.lep.library.dto.deathnotification.audit;

public record DeathEnrichmentAuditExtensions(
    String hashedPayload,
    String correlationID
) {}
