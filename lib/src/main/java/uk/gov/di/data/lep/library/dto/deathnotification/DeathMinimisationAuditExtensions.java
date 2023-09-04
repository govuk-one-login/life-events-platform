package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathMinimisationAuditExtensions(
    String acquiringQueue,
    Integer minimisedPayloadHash
) {
}
