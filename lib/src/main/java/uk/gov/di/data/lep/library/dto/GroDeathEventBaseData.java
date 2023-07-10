package uk.gov.di.data.lep.library.dto;

public record GroDeathEventBaseData(String sourceId) {
    public GroDeathEventBaseData {
        if(sourceId==null) {
            throw new IllegalArgumentException("sourceId cannot be null");
        }
    }
}
