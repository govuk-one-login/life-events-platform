package uk.gov.di.data.lep.library.dto.gro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import uk.gov.di.data.lep.library.config.Constants;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "DeathRegistrationGroup", namespace = Constants.OGD_DEATH_EXTRACT_DWP_NAMESPACE)
public record DeathRegistrationGroup(
    @JsonProperty("DeathRegistration")
    List<GroJsonRecord> deathRegistrations,
    @JsonProperty("RecordCount")
    int recordCount
) {
}
