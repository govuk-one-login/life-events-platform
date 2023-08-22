package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public record DeathRegistrationEventMapping(
    @JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXTERNAL_PROPERTY)
    @JsonSubTypes(value = {
        @JsonSubTypes.Type(value = DeathRegistrationEvent.class, name = "https://ssf.account.gov.uk/v1/deathRegistration"),
        @JsonSubTypes.Type(value = DeathRegistrationUpdateEvent.class, name = "https://ssf.account.gov.uk/v1/deathRegistrationUpdate")
    })
    DeathRegistrationBaseEvent deathRegistrationEvent
) {
}
