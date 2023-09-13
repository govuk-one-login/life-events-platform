package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = DeathRegisteredEvent.class, name = "https://vocab.account.gov.uk/v1/deathRegistered"),
    @JsonSubTypes.Type(value = DeathRegistrationUpdatedEvent.class, name = "https://vocab.account.gov.uk/v1/deathRegistrationUpdated")
})
public interface DeathRegistrationBaseEvent {
    DateWithDescription deathDate();

    Integer deathRegistrationID();

    String freeFormatDeathDate();

    DeathRegistrationSubject subject();
}
