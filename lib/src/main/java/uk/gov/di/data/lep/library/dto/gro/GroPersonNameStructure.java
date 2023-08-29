package uk.gov.di.data.lep.library.dto.gro;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.data.lep.library.config.Constants;

import java.util.List;

public record GroPersonNameStructure(
    @JsonProperty(value = "PersonNameTitle", namespace = Constants.GRO_PERSON_DESCRIPTIVES_NAMESPACE)
    String personNameTitle,
    @JsonProperty(value = "PersonGivenName", namespace = Constants.GRO_PERSON_DESCRIPTIVES_NAMESPACE)
    List<String> personGivenNames,
    @JsonProperty(value = "PersonFamilyName", namespace = Constants.GRO_PERSON_DESCRIPTIVES_NAMESPACE)
    String personFamilyName,
    @JsonProperty(value = "PersonNameSuffix", namespace = Constants.GRO_PERSON_DESCRIPTIVES_NAMESPACE)
    String personNameSuffix
) {
}
