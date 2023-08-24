package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = DeathRegistrationEvent.class, name = "https://ssf.account.gov.uk/v1/deathRegistration"),
    @JsonSubTypes.Type(value = DeathRegistrationUpdateEvent.class, name = "https://ssf.account.gov.uk/v1/deathRegistrationUpdate")
})
public interface DeathRegistrationBaseEvent {
    DateWithDescription deathDate();

    Integer deathRegistrationID();

    String freeFormatDeathDate();

    DeathRegistrationSubject subject();
}
