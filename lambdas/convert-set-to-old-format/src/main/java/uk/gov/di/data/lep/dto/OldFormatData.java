package uk.gov.di.data.lep.dto;

public record OldFormatData(
    String id,
    String type,
    OldFormatDataAttributes attributes,
    Object links,
    Object meta
) {
}
