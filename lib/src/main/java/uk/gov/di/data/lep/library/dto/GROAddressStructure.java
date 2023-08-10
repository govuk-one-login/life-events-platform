package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public record GROAddressStructure(
    @JsonIgnoreProperties(ignoreUnknown = true)
    String Flat,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String Building,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String[] Line,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String Postcode
) {
}
