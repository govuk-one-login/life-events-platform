package uk.gov.di.data.lep.library.dto.DeathNotification;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeathRegistrationEventMapping(
    @JsonProperty("https://ssf.account.gov.uk/v1/deathRegistration")
    DeathRegistrationEvent deathRegistrationEvent,
    @JsonProperty("https://ssf.account.gov.uk/v1/deathRegistrationUpdate")
    DeathRegistrationEvent deathRegistrationUpdateEvent
) {
}
