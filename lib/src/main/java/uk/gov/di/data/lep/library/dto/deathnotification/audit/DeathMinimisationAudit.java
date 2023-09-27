package uk.gov.di.data.lep.library.dto.deathnotification.audit;

import uk.gov.di.data.lep.library.dto.BaseAudit;

public record DeathMinimisationAudit(
    String eventName,
    DeathMinimisationAuditExtensions extensions
) implements BaseAudit {
    public DeathMinimisationAudit(DeathMinimisationAuditExtensions extensions) {
        this("DEATH_MINIMISATION", extensions);
    }
}
