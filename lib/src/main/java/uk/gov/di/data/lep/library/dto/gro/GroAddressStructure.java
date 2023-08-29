package uk.gov.di.data.lep.library.dto.gro;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.di.data.lep.library.config.Constants;

import java.util.List;

public record GroAddressStructure(
    @JsonProperty(value = "Flat", namespace = Constants.GRO_ADDRESS_DESCRIPTIVES_NAMESPACE)
    String flat,
    @JsonProperty(value = "Building", namespace = Constants.GRO_ADDRESS_DESCRIPTIVES_NAMESPACE)
    String building,
    @JsonProperty(value = "Line", namespace = Constants.GRO_ADDRESS_DESCRIPTIVES_NAMESPACE)
    List<String> lines,
    @JsonProperty(value = "Postcode", namespace = Constants.GRO_ADDRESS_DESCRIPTIVES_NAMESPACE)
    String postcode
) {
}
