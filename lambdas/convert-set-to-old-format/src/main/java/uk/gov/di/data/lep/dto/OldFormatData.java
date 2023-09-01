package uk.gov.di.data.lep.dto;

public record OldFormatData(
    String id,
    String type,
    OldFormatDataAttributes attributes,
    Object links,  //TODO: don't care about - unless data is not included?
    Object meta //TODO: don't care about
) {
}
