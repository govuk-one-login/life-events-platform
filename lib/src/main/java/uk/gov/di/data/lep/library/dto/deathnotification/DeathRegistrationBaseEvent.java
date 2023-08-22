package uk.gov.di.data.lep.library.dto.deathnotification;

public interface DeathRegistrationBaseEvent {
    public DateWithDescription deathDate();
    public Integer deathRegistrationID();
    public String freeFormatDeathDate();
    public DeathRegistrationSubject subject();
}
