package uk.gov.di.data.lep.library.dto.deathnotification.audit;

public record DeathMinimisationAuditExtensions(
    String acquiringQueue,
    Integer minimisedPayloadHash,
    String correlationID
) {
}
