package uk.gov.di.data.lep.dto;

public record PartialDateStructure(
    Integer partialMonth,
    Integer partialYear,
    String freeFormatDescription,
    String qualifierText
) {
}
