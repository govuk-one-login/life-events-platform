package uk.gov.di.data.lep.library.exceptions;

public class MappingException extends RuntimeException {
    public MappingException(Exception e) {
        super(e);
    }
}
