package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public record GROPersonNameStructure(
    String PersonNameTitle,
    @JacksonXmlElementWrapper(useWrapping = false)
    List<String> PersonGivenName,
    String PersonFamilyName,
    String PersonNameSuffix,
    String PersonRequestedName
) {
}
