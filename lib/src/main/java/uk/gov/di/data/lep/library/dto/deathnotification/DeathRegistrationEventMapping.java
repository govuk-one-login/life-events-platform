package uk.gov.di.data.lep.library.dto.deathnotification;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeathRegistrationEventMapping(
    @JsonProperty("https://ssf.account.gov.uk/v1/deathRegistration")
    DeathRegistrationEvent deathRegistrationEvent,
    @JsonProperty("https://ssf.account.gov.uk/v1/deathRegistrationUpdate")
    DeathRegistrationUpdateEvent deathRegistrationUpdateEvent
) {
}
