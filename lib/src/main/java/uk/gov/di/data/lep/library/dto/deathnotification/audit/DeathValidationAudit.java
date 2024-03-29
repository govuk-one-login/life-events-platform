package uk.gov.di.data.lep.library.dto.deathnotification.audit;

import uk.gov.di.data.lep.library.dto.BaseAudit;

public record DeathValidationAudit(
    String eventName,
    DeathValidationAuditExtensions extensions
) implements BaseAudit {
    public DeathValidationAudit(DeathValidationAuditExtensions extensions) {
        this("DEATH_VALIDATION", extensions);
    }
}
