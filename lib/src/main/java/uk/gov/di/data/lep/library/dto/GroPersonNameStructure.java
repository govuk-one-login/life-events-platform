package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public record GroPersonNameStructure(
    @JsonProperty("PersonNameTitle")
    String personNameTitle,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("PersonGivenName")
    List<String> personGivenNames,
    @JsonProperty("PersonFamilyName")
    String personFamilyName,
    @JsonProperty("PersonNameSuffix")
    String personNameSuffix,
    @JsonProperty("PersonRequestedName")
    String personRequestedName
) {
}
