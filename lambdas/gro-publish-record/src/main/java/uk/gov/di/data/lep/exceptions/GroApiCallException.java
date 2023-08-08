package uk.gov.di.data.lep.exceptions;

public class GroApiCallException extends RuntimeException{
    public GroApiCallException(String message, Exception e) {
        super(message, e);
    }
}
