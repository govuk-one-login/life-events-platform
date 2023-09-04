package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathEnrichmentAudit(
    String eventName,
    DeathEnrichmentAuditExtensions extensions
) implements BaseAudit {
    public DeathEnrichmentAudit(DeathEnrichmentAuditExtensions extensions) {
        this("DEATH_ENRICHMENT", extensions);
    }
}
