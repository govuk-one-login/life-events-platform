package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record GROPersonNameStructure(
    @JsonIgnoreProperties(ignoreUnknown = true)
    String PersonNameTitle,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String[] PersonGivenName,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String PersonFamilyName,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String PersonNameSuffix,
    @JsonIgnoreProperties(ignoreUnknown = true)
    String PersonRequestedName
) {
}
