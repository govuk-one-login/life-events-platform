package uk.gov.di.data.lep.library.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public record GROAddressStructure(
    String Flat,
    String Building,
    @JacksonXmlElementWrapper(useWrapping = false)
    List<String> Line,
    String Postcode
) {
}
