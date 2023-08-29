package uk.gov.di.data.lep.library.dto.gro;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DeathRegistrationGroup(
    @JsonProperty("DeathRegistration")
    List<GroJsonRecord> deathRegistrations,
    @JsonProperty("RecordCount")
    int recordCount
) {
}
