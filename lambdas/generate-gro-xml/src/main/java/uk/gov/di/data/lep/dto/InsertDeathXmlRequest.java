package uk.gov.di.data.lep.dto;

public record InsertDeathXmlRequest(
    String detailType,
    Integer numberOfRecords
) {
}
