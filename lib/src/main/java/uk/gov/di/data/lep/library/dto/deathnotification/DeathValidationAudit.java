package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathValidationAudit(
    String eventName,
    DeathValidationAuditExtensions extensions
) implements BaseAudit {
    public DeathValidationAudit(DeathValidationAuditExtensions extensions) {
        this("DEATH_VALIDATION", extensions);
    }
}
